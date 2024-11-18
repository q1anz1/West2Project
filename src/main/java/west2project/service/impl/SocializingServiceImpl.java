package west2project.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.context.RedisContexts;
import west2project.exception.ArgsInvalidException;
import west2project.mapper.SocializingMapper;
import west2project.pojo.VO.PageBean;
import west2project.rabbitmq.FlushFriendQueue;
import west2project.result.Result;
import west2project.service.SocializingService;
import west2project.util.JwtUtil;
import west2project.util.PageUtil;
import west2project.util.RedisUtil;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class SocializingServiceImpl implements SocializingService {
    private final SocializingMapper socializingMapper;
    private final HttpServletRequest httpServletRequest;
    private final FlushFriendQueue flushFriendQueue;
    @Override
    public Result<?> action(Long toUserId, Integer type) {
        if(type != 1 && type !=0){
            throw new ArgsInvalidException("操作有误");
        }
        Long userId= JwtUtil.getUserId(httpServletRequest);
        if(Objects.equals(toUserId, userId)){
            throw new ArgsInvalidException("无法关注自己");
        }
        //从缓存中寻找
        Result<?> result= RedisUtil.findJsonListWithCache(RedisContexts.CACHE_FOLLOW,userId, Long.class,socializingMapper::selectUpId,
                RedisContexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> followList = (List<Long>) result.getData();
        if(type==0){
            if(followList.contains(toUserId)){
                throw new ArgsInvalidException("已关注，无法再次关注");
            }
            //存储关注cache
            followList.add(toUserId);
            RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_FOLLOW,userId.toString(),followList, RedisContexts.CACHE_FOLLOW_FAN_TTL);
            //异步存储
            Thread thread = new Thread(() -> socializingMapper.saveFollow(toUserId,userId));
            thread.start();
        }else {
            if(!followList.contains(toUserId)){
                throw new ArgsInvalidException("未关注，无法取关");
            }
            //存储关注cache
            followList.remove(toUserId);
            RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_FOLLOW,userId.toString(),followList, RedisContexts.CACHE_FOLLOW_FAN_TTL);
            //异步存储
            Thread thread = new Thread(() -> socializingMapper.removeFollow(toUserId,userId));
            thread.start();
        }
        //通知兔子刷新朋友表
        flushFriendQueue.sendFlushFriendQueue(userId);
        flushFriendQueue.sendFlushFriendQueue(toUserId);

        return Result.success();
    }

    @Override
    public Result<?> followList(Long userId, Integer pageNum, Integer pageSize) {
        Result<?> result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_FOLLOW,userId, Long.class,socializingMapper::selectUpId,
                RedisContexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            throw new ArgsInvalidException("无关注对象");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            throw new ArgsInvalidException("分页参数非法");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result<?> fanList(Long userId, Integer pageNum, Integer pageSize) {
        Result<?> result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_FAN,userId, Long.class,socializingMapper::selectFanId,
                RedisContexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            throw new ArgsInvalidException("无粉丝");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list,pageSize,pageNum));
        if(pageBean.getData()==null){
            throw new ArgsInvalidException("分页参数非法");
        }
        pageBean.setTotalPage((long) pageBean.getData().size());
        return Result.success(pageBean);
    }

    @Override
    public Result<?> friendList(Integer pageNum, Integer pageSize) {
        Long userId= JwtUtil.getUserId(httpServletRequest);
        Result<?> result = RedisUtil.findJsonListWithCache(RedisContexts.CACHE_FRIEND,userId, Long.class,socializingMapper::selectFriendId,
                RedisContexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            throw new ArgsInvalidException("无朋友");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list,pageSize,pageNum));
        if(pageBean.getData()==null){
            throw new ArgsInvalidException("分页参数非法");
        }
        pageBean.setTotalPage((long) pageBean.getData().size());
        return Result.success(pageBean);
    }

}
