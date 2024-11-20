package west2project.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import west2project.context.RedisContexts;
import west2project.mapper.FriendMapper;
import west2project.mapper.SocializingMapper;
import west2project.pojo.DO.chat.FriendDO;
import west2project.result.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static west2project.context.RedisContexts.CACHE_FRIEND_LIST;
import static west2project.context.RedisContexts.CACHE_FRIEND_LIST_TTL;

@RequiredArgsConstructor
@Component
public class ChatServerUtil {
    private final SocializingMapper socializingMapper;
    private final FriendMapper friendMapper;

    // 刷新数据库中朋友表,并更新redis中的朋友表
    public void flushFriend(Long userId) {
        // 获取表follow中的朋友
        Result<?> result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_FRIEND,userId, Long.class,socializingMapper::selectFriendId, RedisContexts.CACHE_FOLLOW_FAN_TTL);
        Set<Long> followSet = new HashSet<>(((List<Long>)result.getData()));
        // 获取表friend中的朋友
        Set<Long> friendSet = new HashSet<>(friendMapper.selectFriendIdListByUserId(userId));
        // 找出朋友增加了哪些，减少了哪些
        Set<Long> addSet = new HashSet<>(followSet);
        addSet.removeAll(friendSet);
        Set<Long> removeSet = new HashSet<>(friendSet);
        removeSet.removeAll(followSet);
        //将set们变为直接可以插入的List
        List<FriendDO> addList = new ArrayList<>();
        for (Long friendId : addSet) {
            addList.add(new FriendDO(userId,friendId));
        }
        List<FriendDO> remobeList = new ArrayList<>();
        for (Long friendId : removeSet) {
            remobeList.add(new FriendDO(userId,friendId));
        }
        //对friend表进行更新
        if (!addList.isEmpty()) {
            friendMapper.batchInsert(addList);
        }
        if (!remobeList.isEmpty()) {
            friendMapper.batchDelete(remobeList);
        }
        //刷新redis
        RedisUtil.writeJsonWithTTL(CACHE_FRIEND_LIST,userId,followSet,CACHE_FRIEND_LIST_TTL);
    }

}
