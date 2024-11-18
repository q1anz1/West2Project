package west2project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.context.RedisContexts;
import west2project.exception.UserException;
import west2project.mapper.AdminMapper;
import west2project.pojo.DO.user.UserDO;
import west2project.pojo.DO.video.VideoDO;
import west2project.result.Result;
import west2project.service.AdminService;
import west2project.util.RedisUtil;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminMapper adminMapper;

    @Override
    public Result deleteUser(Long userId) {
        //查询是否存在该用户
        UserDO userDO = adminMapper.selectUserDOById(userId);
        if(userDO == null){
            throw new UserException("用户不存在");
        }
        if (userDO.getDeletedAt() != null){
            throw new UserException("用户已被删除");
        }
        Date date = new Date(System.currentTimeMillis());
        //在数据库中逻辑删除，将用户权限改为banned
        adminMapper.deleteUser(userId,date);
        //如果在缓存中存在，则修改缓存
        UserDO cacheUserDO = RedisUtil.findJson(RedisContexts.CACHE_USERDO, userId, UserDO.class);
        if(cacheUserDO != null && cacheUserDO.getUsername() != null){
            userDO.setDeletedAt(date);
            RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_USERDO,userId,userDO, RedisContexts.CACHE_USERDO_TTL);
        }
        return Result.success();
    }

    @Override
    public Result deleteVideo(Long videoId) {
        Date date = new Date(System.currentTimeMillis());
        //在数据库中逻辑删除
        adminMapper.deleteVideo(videoId, date);
        //更新缓存
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_VIDEODO, videoId,
                VideoDO.class, adminMapper::selectVideoDOById, RedisContexts.CACHE_VIDEODO_TTL);
        VideoDO videoDO = (VideoDO) result.getData();
        videoDO.setDeletedAt(date);
        RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_VIDEODO, videoId, videoDO, RedisContexts.CACHE_VIDEODO_TTL);

        return Result.success();
    }
}
