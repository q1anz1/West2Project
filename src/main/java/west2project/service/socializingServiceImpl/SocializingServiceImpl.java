package west2project.service.socializingServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.context.Contexts;
import west2project.mapper.SocializingMapper;
import west2project.pojo.VO.PageBean;
import west2project.result.Result;
import west2project.service.SocializingService;
import west2project.utils.JwtUtil;
import west2project.utils.PageUtil;
import west2project.utils.RedisUtil;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class SocializingServiceImpl implements SocializingService {
    private final SocializingMapper socializingMapper;
    private final HttpServletRequest httpServletRequest;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final PageUtil pageUtil;
    @Override
    public Result action(Long toUserId, Integer type) {
        if(type != 1 && type !=0){
            return Result.error("操作有误");
        }
        Long userId= jwtUtil.getUserId(httpServletRequest);
        if(Objects.equals(toUserId, userId)){
            return Result.error("无法关注自己");
        }
        //从缓存中寻找
        Result result=redisUtil.findJsonListWithCache(Contexts.CACHE_FOLLOW,userId, Long.class,socializingMapper::selectUpId,
                Contexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> followList = (List<Long>) result.getData();
        if(type==0){
            if(followList.contains(toUserId)){
                return Result.error("已关注，无法再次关注");
            }
            //存储关注cache
            followList.add(toUserId);
            redisUtil.writeJsonWithTTL(Contexts.CACHE_FOLLOW,userId.toString(),followList, Contexts.CACHE_FOLLOW_FAN_TTL);
            //异步存储
            Thread thread = new Thread(() -> socializingMapper.saveFollow(toUserId,userId));
            thread.start();
            return Result.success();
        }else if(type==1){
            if(!followList.contains(toUserId)){
                return Result.error("未关注，无法取关");
            }
            //存储关注cache
            followList.remove(toUserId);
            redisUtil.writeJsonWithTTL(Contexts.CACHE_FOLLOW,userId.toString(),followList, Contexts.CACHE_FOLLOW_FAN_TTL);
            //异步存储
            Thread thread = new Thread(() -> socializingMapper.removeFollow(toUserId,userId));
            thread.start();
        }
        return Result.success();
    }

    @Override
    public Result followList(Long userId, Integer pageNum, Integer pageSize) {
        Result result =redisUtil.findJsonListWithCache(Contexts.CACHE_FOLLOW,userId, Long.class,socializingMapper::selectUpId,
                Contexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            return Result.error("无关注对象");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(pageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            return Result.error("分页参数非法");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result fanList(Long userId, Integer pageNum, Integer pageSize) {
        Result result =redisUtil.findJsonListWithCache(Contexts.CACHE_FAN,userId, Long.class,socializingMapper::selectFanId,
                Contexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            return Result.error("无粉丝");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(pageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            return Result.error("分页参数非法");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result friendList(Integer pageNum, Integer pageSize) {
        Long userId= jwtUtil.getUserId(httpServletRequest);
        Result result =redisUtil.findJsonListWithCache(Contexts.CACHE_FRIEND,userId, Long.class,socializingMapper::selectFriendId,
                Contexts.CACHE_FOLLOW_FAN_TTL);
        List<Long> list= (List<Long>) result.getData();
        if(list.isEmpty()){
            return Result.error("无朋友");
        }
        PageBean pageBean = new PageBean<>();
        pageBean.setData(pageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            return Result.error("分页参数非法");
        }
        return Result.success(pageBean);
    }
}
