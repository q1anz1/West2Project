package west2project.util;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import west2project.mapper.*;
import west2project.pojo.DO.chat.GroupDO;
import west2project.pojo.DO.chat.SessionDO;
import west2project.pojo.VO.chat.GroupVO;
import west2project.pojo.VO.chat.SessionVO;
import west2project.pojo.VO.chat.message.FullMessage;
import west2project.pojo.VO.chat.message.WsInitMsg;
import west2project.pojo.VO.user.UserInfoVO;
import west2project.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static west2project.context.RedisContexts.*;
import static west2project.pojo.VO.chat.message.FullMessage.INIT_WS_MSG;
import static west2project.pojo.VO.chat.message.FullMessage.SYSTEM_ID;

@Component
@RequiredArgsConstructor
public class ChannelUtil {
    private static final ConcurrentHashMap<Long, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    private final GroupUserMapper groupUserMapper;
    private final SessionMapper sessionMapper;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;
    private final FriendMapper friendMapper;

    // 是否在线
    public static boolean isUserOnline(Long userId) {
        return USER_CONTEXT_MAP.containsKey(userId);
    }

    // 发送消息
    public static boolean sendMsg(Result<?> result, Long receiverId) {
        if (receiverId == null) return false;
        Channel channel = USER_CONTEXT_MAP.get(receiverId);
        if (channel == null) return false;
        channel.writeAndFlush(new TextWebSocketFrame(result.asJsonString()));
        return true;
    }

    // 在通道升级协议后初始化通道
    public void initChannel(Long userId, Channel channel) {
        addUserIdToChannelAttribute(userId, channel);
        onlineChannel(userId, channel);
        // 查询用户的联系人，群和会话
        WsInitMsg wsInitMsg = getWsInitVO(userId);
        // 返回前端
        sendMsg(Result.success(FullMessage.init(INIT_WS_MSG,SYSTEM_ID,wsInitMsg)),userId);
    }

    // 移除在线用户并关闭
    public void removeChannel(Channel channel) {
        Attribute<Long> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        Long userId = attribute.get();
        offlineChannel(userId, channel);
        channel.close();
    }

    // 初始化后要返回前端的消息
    private WsInitMsg getWsInitVO(Long userId) {
        WsInitMsg wsInitMsg = new WsInitMsg();
        // 最后一条消息在最近30天内的session
        wsInitMsg.setSessionVOList(getSessionVOList(userId));
        // 好友列表
        wsInitMsg.setFriendUserInfoVOList(getUserInfoVOList(userId));
        // 群聊列表
        wsInitMsg.setGroupVOList(getGroupVO(userId));
        return wsInitMsg;
    }

    // 返回最后一条消息在最近30天内的所有群和好友的session
    private List<SessionVO> getSessionVOList(Long userId) {
        List<SessionVO> sessionVOList = new ArrayList<>();
        for (SessionDO sDO : sessionMapper.selectSessionDOListLast30DaysByUserId(userId)) {
            if (sDO.getGroupId() != null) {
                // 是群session
                Result<?> result = RedisUtil.findJsonWithCache(CACHE_GROUPDO, sDO.getGroupId(), GroupDO.class, groupMapper::selectGroupDOByGroupId, CACHE_GROUPDO_TTL);
                GroupDO groupDO = (GroupDO) result.getData();
                // 加入列表
                sessionVOList.add( new SessionVO(sDO.getId(), null, sDO.getGroupId(), groupDO.getGroupName(), groupDO.getAvatarUrl(), sDO.getLastMessage(), sDO.getUpdatedAt()));
            } else {
                // 是好友session
                Result<?> result;
                if (Objects.equals(sDO.getUserId1(), userId)) {
                    // userId1是本人
                    result = RedisUtil.findJsonWithCache(CACHE_USER_INFO, sDO.getUserId2(), UserInfoVO.class, userMapper::findUserInfoVOByUserId, CACHE_USER_INFO_TTL);
                } else {
                    // userId2是本人
                    result = RedisUtil.findJsonWithCache(CACHE_USER_INFO, sDO.getUserId1(), UserInfoVO.class, userMapper::findUserInfoVOByUserId, CACHE_USER_INFO_TTL);
                }
                UserInfoVO user = (UserInfoVO) result.getData();
                sessionVOList.add(new SessionVO(sDO.getId(), user.getId(), null, user.getUsername(), user.getAvatarUrl(), sDO.getLastMessage(), sDO.getUpdatedAt()));
            }
        }
        return sessionVOList;
    }

    // 返回该用户所有的彭油，爱情不是冰红茶
    private List<UserInfoVO> getUserInfoVOList(Long userId) {
        List<UserInfoVO> friendUserInfoList = new ArrayList<>();
        for (Long friendId: friendMapper.selectFriendIdByUserId(userId)) {
            Result<?> result = RedisUtil.findJsonWithCache(CACHE_USER_INFO, friendId, UserInfoVO.class, userMapper::findUserInfoVOByUserId, CACHE_USER_INFO_TTL);
            friendUserInfoList.add((UserInfoVO) result.getData());
        }
        return friendUserInfoList;
    }

    // 返回该用户所有群聊
    private List<GroupVO> getGroupVO(Long userId) {
        List<GroupVO> groupVOList  = new ArrayList<>();
        for (GroupDO DO: groupMapper.selectGroupDOListByUserId(userId)) {
            groupVOList.add(new GroupVO(DO.getId(), DO.getGroupName(), DO.getLeaderId(), DO.getText() ,DO.getAvatarUrl() ,DO.getCount()));
        }
        return groupVOList;
    }

    // 将userId加入channel的attribute
    private void addUserIdToChannelAttribute(Long userId, Channel channel) {
        String channelId = channel.id().toString();
        AttributeKey<Long> attributeKey;
        if (AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
    }

    // 使channel完成对GROUP_CONTEXT_MAP和USER_CONTEXT_MAP的加入
    private void onlineChannel(Long userId, Channel channel) {
        // 用户上线
        USER_CONTEXT_MAP.put(userId, channel);
        // 组内上线
        List<Long> groupIdList = groupUserMapper.selectGroupIdListByUserId(userId);
        for (Long groupId : groupIdList) {
            addChannelToGroup(groupId, channel);
        }
    }

    private void offlineChannel(Long userId, Channel channel) {
        if (userId != null) {
            // 用户下线
            USER_CONTEXT_MAP.remove(userId);
            // 组内下线
            List<Long> groupIdList = groupUserMapper.selectGroupIdListByUserId(userId);
            for (Long groupId : groupIdList) {
                removeChannelToGroup(groupId, channel);
            }
        }
    }

    // 将channel加入GROUP_CONTEXT_MAP中对应groupId的channelGroup
    private void addChannelToGroup(Long groupId, Channel channel) {
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(groupId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, channelGroup);
        }
        if (channel == null) {
            return;
        }
        channelGroup.add(channel);
    }

    // 将channel移除出GROUP_CONTEXT_MAP中对应groupId的channelGroup
    private void removeChannelToGroup(Long groupId, Channel channel) {
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(groupId);
        if (channelGroup == null || channel == null) {
            return;
        }
        channelGroup.remove(channel);
    }
}
