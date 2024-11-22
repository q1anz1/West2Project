package west2project.chat.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.exception.ArgsInvalidException;
import west2project.exception.UserException;
import west2project.mapper.*;
import west2project.pojo.DO.chat.GroupDO;
import west2project.pojo.DO.chat.MessageDO;
import west2project.pojo.DO.chat.SessionDO;
import west2project.pojo.VO.chat.SessionVO;
import west2project.pojo.VO.chat.message.ChatMsg;
import west2project.pojo.VO.chat.message.FullMessage;
import west2project.pojo.VO.user.UserInfoVO;
import west2project.rabbitmq.ChatMessageQueue;
import west2project.rabbitmq.SaveChatMsgQueue;
import west2project.rabbitmq.UpdateSessionQueue;
import west2project.result.Result;
import west2project.util.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static west2project.context.CommonContexts.CHAT_IMAGE_BOX;
import static west2project.context.RedisContexts.*;
import static west2project.pojo.VO.chat.message.FullMessage.CHAT_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final HttpServletRequest httpServletRequest;
    private final ChannelUtil channelUtil;
    private final FriendMapper friendMapper;
    private final SessionMapper sessionMapper;
    private final UserMapper userMapper;
    private final GroupUserMapper groupUserMapper;
    private final GroupMapper groupMapper;
    private final SaveChatMsgQueue saveChatMsgQueue;
    private final ChatMessageQueue chatMessageQueue;
    private final UpdateSessionQueue updateSessionQueue;
    @Override
    public Result<?> sendMessage(Long sessionId, String text, MultipartFile picture) {
        // 拒绝以下请求
        if (text == null && (picture == null || picture.isEmpty())) {
            throw new ArgsInvalidException("参数错误");
        }
        // 得到当前用户id
        Long userId = JwtUtil.getUserId(httpServletRequest);
        assert userId != null;
        // 从session获得对话信息
        SessionDO sessionDO = (SessionDO) RedisUtil.findJsonWithCache(CACHE_SESSIONDO, sessionId, SessionDO.class, sessionMapper::selectSessionDOBySessionId, CACHE_SESSIONDO_TTL).getData();
        if (sessionDO == null) throw new RuntimeException("会话不存在");
        // 验证会话是否属于该用户
        Long groupId = null;
        Long toUserId = null;
        if (sessionDO.getGroupId() == null) {
            // 是好友间的消息
            if (!(isFriend(userId, sessionDO.getUserId1()) || isFriend(userId, sessionDO.getUserId2()))) {
                throw new ArgsInvalidException("不是好友");
            }
            toUserId = sessionDO.getUserId1().equals(userId)?sessionDO.getUserId2():sessionDO.getUserId1();
        } else {
            // 是群消息
            groupId = sessionDO.getGroupId();
            if (!isInGroup(userId, groupId)) {
                throw new ArgsInvalidException("不在群内");
            }
        }
        // 验证消息文本和图片合法性
        if (text != null) ChatUtil.isMsgValid(text);
        String isImageValid = TikaUtil.isImageValid(picture);
        if (!(isImageValid.equals("true") || isImageValid.equals("文件是空的"))) {
            throw new ArgsInvalidException(isImageValid);
        }
        //将图片保存至本地
        String pictureUrl = null;
        if (!picture.isEmpty()) {
            pictureUrl = SaveUtil.saveFile(picture, CHAT_IMAGE_BOX);
        }
        // 打包为用于推送的消息
        Date createdAt = new Date(System.currentTimeMillis());
        FullMessage<ChatMsg> fullMessage = FullMessage.init(CHAT_MSG, userId, new ChatMsg(text, pictureUrl, createdAt));
        // 推送消息
        if (toUserId == null) {
            // 发送消息
            chatMessageQueue.sendChatMessageQueue(fullMessage, groupId, false);
        } else {
            // 发送消息
            chatMessageQueue.sendChatMessageQueue(fullMessage, toUserId, true);
        }
        // 将消息存入数据库(mq)
        saveChatMsgQueue.sendSaveChatMsgQueue(new MessageDO(userId, text, pictureUrl, toUserId, groupId, createdAt));
        // 更新session于redis和db
        if (text != null && text.length()>10) text = text.substring(0, 10);
        sessionDO.setLastMessage(text);
        sessionDO.setUpdatedAt(createdAt);
        updateSessionQueue.sendUpdateSessionQueue(sessionDO);
        return Result.success();
    }

    @Override
    public Result<?> getSessionList() {
        Long userId = JwtUtil.getUserId(httpServletRequest);
        return Result.success(channelUtil.getSessionVOList(userId));
    }

    @Override
    public Result<?> newSession(Long toUserId, Long groupId) {
        // 返回有误请求
        if ((toUserId == null && groupId == null) || (toUserId != null && groupId != null))
            throw new ArgsInvalidException("请求有误");
        // 得到id
        Long userId = JwtUtil.getUserId(httpServletRequest);
        assert userId != null;

        // 新建会话
        SessionVO sessionVO;
        if (toUserId == null) {
            // 群聊会话
            // 查看是否存在
            SessionDO ifExistSessionDO = sessionMapper.selectSessionDOByUserIdGroupId(userId, groupId);
            if (ifExistSessionDO != null) return Result.success(ifExistSessionDO);
            // 是否是群成员
            Result<?> result = RedisUtil.findJsonListWithCache(CACHE_GROUP_LIST, userId, Long.class, groupUserMapper::selectGroupIdListByUserId, CACHE_GROUP_LIST_TTL);
            List<Long> groupIdList = (List<Long>) result.getData();
            if (!groupIdList.contains(groupId)) throw new UserException("不是群成员，无法创建会话");
            // 创建会话，保存到数据库，这里mapper自动将sessionId存入do了
            Date updatedAt = new Date(System.currentTimeMillis());
            SessionDO sessionDO = new SessionDO(userId, null, groupId, "无消息", updatedAt);
            sessionMapper.saveSessionDO(sessionDO);
            // 查询群头像和名字
            GroupDO groupDO = (GroupDO) RedisUtil.findJsonWithCache(CACHE_GROUPDO, groupId, GroupDO.class, groupMapper::selectGroupDOByGroupId, CACHE_GROUPDO_TTL).getData();
            if (groupDO == null) throw new UserException("读取群资料信息出错");
            sessionVO = new SessionVO(sessionDO.getId(), null, groupId, groupDO.getGroupName(), groupDO.getAvatarUrl(), sessionDO.getLastMessage(), sessionDO.getUpdatedAt());
        } else {
            // 查看是否存在
            SessionDO ifExistSessionDO = sessionMapper.selectSessionDOByUserId1UserId2(userId, toUserId);
            if (ifExistSessionDO != null) return Result.success(ifExistSessionDO);
            // 是否为好友
            Result<?> result = RedisUtil.findJsonListWithCache(CACHE_FRIEND_LIST, userId, Long.class, friendMapper::selectFriendIdListByUserId, CACHE_FRIEND_LIST_TTL);
            List<Long> friendIdList = (List<Long>) result.getData();
            if (!friendIdList.contains(toUserId)) throw new UserException("不是好友，无法创建会话");
            // 创建会话，保存到数据库，这里mapper自动将sessionId存入do了
            Date updatedAt = new Date(System.currentTimeMillis());
            SessionDO sessionDO = new SessionDO(userId, toUserId, null, "无消息", updatedAt);
            sessionMapper.saveSessionDO(sessionDO);
            // 查询对方用户头像和名字
            UserInfoVO userInfoVO = (UserInfoVO) RedisUtil.findJsonWithCache(CACHE_USER_INFO, toUserId, UserInfoVO.class, userMapper::findUserInfoVOByUserId, CACHE_USER_INFO_TTL).getData();
            if (userInfoVO == null) throw new UserException("读取用户信息出错");
            // 返回前端
            sessionVO = new SessionVO(sessionDO.getId(), sessionDO.getUserId2(), null, userInfoVO.getUsername(), userInfoVO.getAvatarUrl(), sessionDO.getLastMessage(), sessionDO.getUpdatedAt());
        }
        return Result.success(sessionVO);
    }

    private boolean isFriend(Long userId, Long toUserId) {
        if (Objects.equals(userId, toUserId)) return true;
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_FRIEND_LIST, userId, Long.class, friendMapper::selectFriendIdListByUserId, CACHE_FRIEND_LIST_TTL);
        List<Long> friendIdList = (List<Long>) result.getData();
        return friendIdList.contains(toUserId);
    }

    private boolean isInGroup(Long userId, Long groupId) {
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_GROUP_LIST, userId, Long.class, groupUserMapper::selectGroupIdListByUserId, CACHE_GROUPDO_TTL);
        List<Long> groupIdList = (List<Long>) result.getData();
        return groupIdList.contains(groupId);
    }
}
