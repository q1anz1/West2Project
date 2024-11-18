package west2project.chat.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.exception.ArgsInvalidException;
import west2project.mapper.FriendMapper;
import west2project.pojo.DO.chat.MessageDO;
import west2project.pojo.VO.chat.message.ChatMsg;
import west2project.pojo.VO.chat.message.FullMessage;
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
    private final SaveChatMsgQueue saveChatMsgQueue;
    private final RedisUtil redisUtil;

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
        if(!picture.isEmpty()) {
            pictureUrl = SaveUtil.saveFile(picture, CHAT_IMAGE_BOX);
        }
        //得到当前用户id
        Long userId = JwtUtil.getUserId(httpServletRequest);
        // 将消息存入数据库(mq)
        Date createdAt = new Date(System.currentTimeMillis());
        saveChatMsgQueue.sendSaveChatMsgQueue(new MessageDO(userId, text, pictureUrl, toUserId, groupId, createdAt));
        // 打包为用于推送的消息
        FullMessage<ChatMsg> fullMessage = FullMessage.init(CHAT_MSG, userId, new ChatMsg(text,pictureUrl,createdAt));
        // 推送消息
        if (toUserId == null) {
            // TODO 处理群消息

        } else {
            // TODO 处理好友消息
            if (isFriend(userId, toUserId)) {
                return Result.error(CODE_400.getCode(), "不是好友");
            }
            // 通过websocket发送给目标用户，如果发送成功返回true
            boolean isSuccess =  ChannelUtil.sendMsg(Result.success(fullMessage),toUserId);
            // 好友不在线
            if (!isSuccess) {
                // 将消息存到redis
                redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, toUserId, fullMessage);
            }
        }
        return Result.success();
    }

    private boolean isFriend(Long userId, Long toUserId) {
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_FRIEND_LIST, userId, Long.class, friendMapper::selectFriendIdByUserId, CACHE_FRIEND_LIST_TTL);
        List<Long> friendIdList = (List<Long>) result.getData();
        return friendIdList.contains(toUserId);
    }
}
