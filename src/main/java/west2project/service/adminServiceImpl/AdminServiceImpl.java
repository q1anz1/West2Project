package west2project.service.adminServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.context.Contexts;
import west2project.mapper.AdminMapper;
import west2project.pojo.DO.users.UserDO;
import west2project.pojo.DO.videos.VideoDO;
import west2project.result.Result;
import west2project.service.AdminService;
import west2project.utils.RedisUtil;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminMapper adminMapper;
    private final RedisUtil redisUtil;

    @Override
    public Result deleteUser(Long userId) {
        //查询是否存在该用户
        UserDO userDO = adminMapper.selectUserDOById(userId);
        if(userDO == null){
            return Result.error("用户不存在");
        }
        if (userDO.getDeletedAt() != null){
            return Result.error("用户已被删除");
        }
        Date date = new Date(System.currentTimeMillis());
        //在数据库中逻辑删除，将用户权限改为banned
        adminMapper.deleteUser(userId,date);
        //如果在缓存中存在，则修改缓存
        UserDO cacheUserDO = redisUtil.findJson(Contexts.CACHE_USERDO, userId, UserDO.class);
        if(cacheUserDO != null && cacheUserDO.getUsername() != null){
            userDO.setDeletedAt(date);
            redisUtil.writeJsonWithTTL(Contexts.CACHE_USERDO,userId,userDO, Contexts.CACHE_USERDO_TTL);
        }
        return Result.success();
    }

    @Override
    public Result deleteVideo(Long videoId) {
        Date date = new Date(System.currentTimeMillis());
        //在数据库中逻辑删除
        adminMapper.deleteVideo(videoId, date);
        //更新缓存
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_VIDEODO, videoId,
                VideoDO.class, adminMapper::selectVideoDOById, Contexts.CACHE_VIDEODO_TTL);
        VideoDO videoDO = (VideoDO) result.getData();
        videoDO.setDeletedAt(date);
        redisUtil.writeJsonWithTTL(Contexts.CACHE_VIDEODO, videoId, videoDO, Contexts.CACHE_VIDEODO_TTL);

        return Result.success();
    }
}
