package west2project.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import west2project.exception.ArgsInvalidException;
import west2project.exception.WebSocketArgsInvalidException;
import west2project.mapper.FriendMapper;
import west2project.mapper.GroupUserMapper;
import west2project.mapper.SessionMapper;
import west2project.pojo.DO.chat.MessageDO;
import west2project.pojo.DO.chat.SessionDO;
import west2project.pojo.DTO.chat.MQSaveChatMsgDTO;
import west2project.pojo.VO.chat.message.ChatMsg;
import west2project.pojo.VO.chat.message.FullMessage;
import west2project.rabbitmq.SaveChatMsgQueue;
import west2project.rabbitmq.UpdateSessionQueue;
import west2project.result.Result;
import west2project.util.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static west2project.context.CommonContexts.CHAT_IMAGE_BOX;
import static west2project.context.RedisContexts.*;
import static west2project.pojo.VO.chat.message.FullMessage.CHAT_MSG;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChannelRead {
    private final SaveChatMsgQueue saveChatMsgQueue;
    private final UpdateSessionQueue updateSessionQueue;
    private final SessionMapper sessionMapper;
    private final FriendMapper friendMapper;
    private final RedisUtil redisUtil;
    private final GroupUserMapper groupUserMapper;

    public void handle(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
        Channel channel = ctx.channel();
        Attribute<Long> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        Long senderId = attribute.get();
        String msg = textWebSocketFrame.text();
        log.info("userId:{}, send:{}", senderId, msg.length()<20?msg:msg.substring(0,20));
        // 各种指令的处理器
        Result<?> result;
        try {
            result = sendTextMessageHandle(senderId, msg);
            result = sendPictureMessageHandle(senderId, msg);
        } catch (ArgsInvalidException e) {
            throw new WebSocketArgsInvalidException(e.getMessage());
        }
        if (result == null) throw new WebSocketArgsInvalidException("指令出错");
        ctx.writeAndFlush(new TextWebSocketFrame(result.asJsonString()));
    }

    private Result<?> sendTextMessageHandle(Long senderId, String msg) {
        if (noMatcher("sendText [0-9]+ .*", msg)) return null;
        long targetId;
        try {
            // 去除指令
            msg = msg.substring("sendText ".length());
            // 得到目标id
            int index = 0;
            for (int i = 0; i < msg.length(); i++) {
                if (msg.charAt(i) == ' ') {
                    index = i;
                    break;
                }
            }
            targetId = Long.parseLong(msg.substring(0, index));
            // 得到消息
            msg = msg.substring(index + 1);
        } catch (RuntimeException e) {
            throw new ArgsInvalidException("指令解析出错");
        }
        return sendMessage(senderId, targetId, msg, null);
    }

    private Result<?> sendPictureMessageHandle(Long senderId, String msg) {
        if (noMatcher("sendPicture [0-9]+ .*", msg)) return null;
        long targetId;
        try {
            // 去除指令
            msg = msg.substring("sendPicture ".length());
            // 得到目标id
            int index = 0;
            for (int i = 0; i < msg.length(); i++) {
                if (msg.charAt(i) == ' ') {
                    index = i;
                    break;
                }
            }
            targetId = Long.parseLong(msg.substring(0, index));
            // 得到消息
            msg = msg.substring(index + 1);
        } catch (RuntimeException e) {
            throw new ArgsInvalidException("指令解析出错");
        }
        // 把base64变成文件
        MultipartFile multipartFile = SaveUtil.base64ToImage(msg);
        return sendMessage(senderId, targetId, null, multipartFile);
    }

    private boolean noMatcher(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return !matcher.matches();
    }

    public Result<?> sendMessage(Long senderId, Long sessionId, String text, MultipartFile picture) {
        // 拒绝以下请求
        if (text == null && (picture == null || picture.isEmpty())) {
            throw new ArgsInvalidException("参数错误");
        }
        // 得到当前用户id
        assert senderId != null;
        // 从session获得对话信息
        SessionDO sessionDO = (SessionDO) RedisUtil.findJsonWithCache(CACHE_SESSIONDO, sessionId, SessionDO.class, sessionMapper::selectSessionDOBySessionId, CACHE_SESSIONDO_TTL).getData();
        if (sessionDO == null) throw new RuntimeException("会话不存在");
        // 验证会话是否属于该用户
        Long groupId = null;
        Long toUserId = null;
        if (sessionDO.getGroupId() == null) {
            // 是好友间的消息
            if (!(isFriend(senderId, sessionDO.getUserId1()) || isFriend(senderId, sessionDO.getUserId2()))) {
                throw new ArgsInvalidException("不是好友");
            }
            toUserId = sessionDO.getUserId1().equals(senderId) ? sessionDO.getUserId2() : sessionDO.getUserId1();
        } else {
            // 是群消息
            groupId = sessionDO.getGroupId();
            if (!isInGroup(senderId, groupId)) {
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
        if (!(picture == null || picture.isEmpty())) {
            pictureUrl = SaveUtil.saveFile(picture, CHAT_IMAGE_BOX);
        }
        // 打包为用于推送的消息
        Date createdAt = new Date(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        FullMessage<ChatMsg> fullMessage = FullMessage.init(CHAT_MSG, senderId, groupId, new ChatMsg(uuid, text, pictureUrl, createdAt));
        // 推送消息
        if (toUserId == null) {
            // 群发送消息
            sendChatMsg(fullMessage, groupId, false);
        } else {
            // 好友发送消息
            sendChatMsg(fullMessage, toUserId, true);
        }
        // 将消息存入数据库(mq)
        MQSaveChatMsgDTO mqSaveChatMsgDTO = new MQSaveChatMsgDTO(new MessageDO(senderId, text, pictureUrl, toUserId, groupId, createdAt), uuid);
        saveChatMsgQueue.sendSaveChatMsgQueue(mqSaveChatMsgDTO);
        // 更新session于redis和db
        if (text != null && text.length() > 10) text = text.substring(0, 10);
        sessionDO.setLastMessage(text);
        sessionDO.setUpdatedAt(createdAt);
        updateSessionQueue.sendUpdateSessionQueue(sessionDO);
        return Result.success();
    }

    private void sendChatMsg(FullMessage<ChatMsg> fullMessage, Long targetId, Boolean toUser) {
        // 通过websocket发送给目标用户，如果发送成功返回true
        if (toUser) {
            // 发送给好友
            // 通过websocket发送给目标用户，如果发送成功返回true
            boolean isSuccess = ChannelUtil.sendPersonalMsg(Result.success(fullMessage), targetId);
            // 好友不在线
            if (!isSuccess) {
                // 将消息存到redis
                redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, targetId, fullMessage);
            }
        } else {
            // 发送到群
            // 通过websocket发送给目标用户，如果发送成功返回true
            List<Long> successUserIdList = ChannelUtil.sendGroupMessage(Result.success(fullMessage), targetId);
            // 群友不在线
            // 获得群友id列表
            Result<?> result = RedisUtil.findJsonListWithCache(CACHE_GROUP_USER, targetId, Long.class, groupUserMapper::selectGroupUserIdByGroupId, CACHE_GROUP_USER_TTL);
            List<Long> userIdList = (List<Long>) result.getData();
            // 获得发送未成功的群友id列表
            successUserIdList.forEach(userIdList::remove);
            List<Long> unsuccessUserIdList = new ArrayList<>(successUserIdList);
            // 存储到redis
            unsuccessUserIdList.forEach(userid -> redisUtil.rightPushList(REDIS_UNREAD_MESSAGE, userid, fullMessage));
        }
    }

    private boolean isFriend(Long userId, Long toUserId) {
        if (Objects.equals(userId, toUserId)) return true;
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_FRIEND_LIST, userId, Long.class, friendMapper::selectFriendIdListByUserId, CACHE_FRIEND_LIST_TTL);
        List<Long> friendIdList = (List<Long>) result.getData();
        if (friendIdList.isEmpty()) return false;
        return friendIdList.contains(toUserId);
    }

    private boolean isInGroup(Long userId, Long groupId) {
        Result<?> result = RedisUtil.findJsonListWithCache(CACHE_GROUP_LIST, userId, Long.class, groupUserMapper::selectGroupIdListByUserId, CACHE_GROUPDO_TTL);
        List<Long> groupIdList = (List<Long>) result.getData();
        if (groupIdList.isEmpty()) return false;
        return groupIdList.contains(groupId);
    }

}
