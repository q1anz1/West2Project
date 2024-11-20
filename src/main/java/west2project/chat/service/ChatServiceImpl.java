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
import west2project.rabbitmq.SaveChatMsgQueue;
import west2project.result.Result;
import west2project.util.*;

import java.util.Date;
import java.util.List;

import static west2project.context.CommonContexts.CHAT_IMAGE_BOX;
import static west2project.context.RedisContexts.*;
import static west2project.enums.ResponseCodeEnum.CODE_400;
import static west2project.pojo.VO.chat.message.FullMessage.CHAT_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final HttpServletRequest httpServletRequest;
    private final FriendMapper friendMapper;
    private final SessionMapper sessionMapper;
    private final UserMapper userMapper;
    private final GroupUserMapper groupUserMapper;
    private final GroupMapper groupMapper;
    private final SaveChatMsgQueue saveChatMsgQueue;
    private final RedisUtil redisUtil;
    private final ChannelUtil channelUtil;

    @Override
    public Result<?> sendMessage(Long toUserId, Long groupId, String text, MultipartFile picture) {
        // 拒绝以下请求
        if ((toUserId == null && groupId == null) || (toUserId != null && groupId != null) || (text == null && (picture == null || picture.isEmpty()))) {
            throw new ArgsInvalidException("参数错误");
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
        //得到当前用户id
        Long userId = JwtUtil.getUserId(httpServletRequest);
        // 打包为用于推送的消息
        Date createdAt = new Date(System.currentTimeMillis());
        FullMessage<ChatMsg> fullMessage = FullMessage.init(CHAT_MSG, userId, new ChatMsg(text, pictureUrl, createdAt));
        // 推送消息
        if (toUserId == null) {
            // TODO 处理群消息

        } else {
            // TODO 处理好友消息
            if (!isFriend(userId, toUserId)) {
                return Result.error(CODE_400.getCode(), "不是好友");
            }
            // 通过websocket发送给目标用户，如果发送成功返回true
            boolean isSuccess = ChannelUtil.sendMsg(Result.success(fullMessage), toUserId);
            // 好友不在线
            if (!isSuccess) {
                // 将消息存到redis
                redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, toUserId, fullMessage);
            }
        }
        // 将消息存入数据库(mq)
        saveChatMsgQueue.sendSaveChatMsgQueue(new MessageDO(userId, text, pictureUrl, toUserId, groupId, createdAt));
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
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_FRIEND_LIST, userId, Long.class, friendMapper::selectFriendIdListByUserId, CACHE_FRIEND_LIST_TTL);
        List<Long> friendIdList = (List<Long>) result.getData();
        return friendIdList.contains(toUserId);
    }
}
