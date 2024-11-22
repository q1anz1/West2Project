package west2project.service.impl;

import cn.hutool.core.date.DateTime;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.context.CommonContexts;
import west2project.context.RedisContexts;
import west2project.exception.ArgsInvalidException;
import west2project.mapper.VideoMapper;
import west2project.pojo.DO.video.VideoDO;
import west2project.pojo.DTO.video.VideoInfoDTO;
import west2project.pojo.VO.PageBean;
import west2project.rabbitmq.SaveDBQueues;
import west2project.result.Result;
import west2project.service.VideoService;
import west2project.util.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private final VideoMapper videoMapper;
    private final HttpServletRequest httpServletRequest;
    private final SaveDBQueues saveDBQueues;
    @Override
    public Result publish(MultipartFile video, String title, MultipartFile image, String description) {
        //验证视频合法性并修正
        String isVideoValid = TikaUtil.isVideoValid(video, title);
        if (!isVideoValid.equals("true")) {
            throw new ArgsInvalidException(isVideoValid);
        }
        if (title.length() > 20) {
            throw new ArgsInvalidException("标题字数过多");
        }
        if (description == null || description.isEmpty()) {
            description = "-";
        }
        if (description.length() > 200) {
            throw new ArgsInvalidException("描述字数过多");
        }
        //验证封面合法性
        String isImageValid = TikaUtil.isImageValid(image);
        if (!isImageValid.equals("true")) {
            throw new ArgsInvalidException(isImageValid);
        }
        //存入本地
        String videoUrl = SaveUtil.saveFileWithName(video, CommonContexts.VIDEO_BOX, SaveUtil.changeFileName(video));
        String imageUrl = SaveUtil.saveFileWithName(image, CommonContexts.COVER_BOX, SaveUtil.changeFileName(image));
        //获取用户id
        Long userId = JwtUtil.getUserId(JwtUtil.getJwt(httpServletRequest));
        //存入数据库
        videoMapper.saveVideo(userId, videoUrl, imageUrl, title, description, DateTime.now(), DateTime.now());
        //将上传信息存入redis
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_PUBLISH_VIDEO_LIST, userId, List.class, videoMapper::findVideoPublishListByUserId,
                RedisContexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        List<Long> list = (List<Long>) result.getData();
        list.add(videoMapper.findVideoIdByVideoUrl(videoUrl));
        RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_PUBLISH_VIDEO_LIST, userId.toString(), list, RedisContexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        return Result.success();
    }

    @Override
    public Result videoPublishList(Long userId, Integer pageNum, Integer pageSize) {
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_PUBLISH_VIDEO_LIST, userId, List.class, videoMapper::findVideoPublishListByUserId,
                RedisContexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        List<Long> list = (List<Long>) result.getData();
        if (list.isEmpty()) {
            throw new ArgsInvalidException("无视频");
        }
        List<Long> pageBean = PageUtil.page(list, pageSize, pageNum);
        if (pageBean == null) {
            throw new ArgsInvalidException("分页参数非法");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result searchVideo(String keywords, Integer pageSize, Integer pageNum, Integer fromDate, Integer toDate, String username) {
        //将timestamp转为date
        Date date1 = null;
        Date date2 = null;
        if (fromDate != null) {
            date1 = new Date(fromDate);

        }
        if (toDate != null) {
            date2 = new Date(toDate);
        }
        //将用户名转化为id
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_USERNAME_TO_USERID, username, Long.class,
                videoMapper::findUserIdByUsername, RedisContexts.CACHE_USERNAME_TO_USERID_TTL);
        Long userId = (Long) result.getData();
        //在数据库进行查询
        PageHelper.startPage(pageNum, pageSize);
        List<VideoInfoDTO> list = videoMapper.searchVideoId(keywords,date1, date2, userId,pageSize,(pageNum-1)*pageSize);
        PageInfo<VideoInfoDTO> p = new PageInfo<>(list);
        //创建pageBean
        PageBean pageBean = new PageBean<>();
        pageBean.setTotalPage(p.getTotal());
        pageBean.setData(p.getList());
        if (pageBean.getData() == null || pageBean.getData().isEmpty()) {
            throw new ArgsInvalidException("无结果");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result videoFeed(Long videoId) {
        Result result = RedisUtil.findJsonWithCache(RedisContexts.CACHE_VIDEODO,videoId, VideoDO.class,videoMapper::findVideoDOByVideoId,
                RedisContexts.CACHE_VIDEODO_TTL);
        VideoDO videoDO = (VideoDO) result.getData();
        if(videoDO==null){
            throw new ArgsInvalidException("视频不存在");
        }
        if(videoDO.getDeletedAt() != null || !videoDO.getReview()){
            throw new ArgsInvalidException("视频不见了");
        }
        //加入 有人看过队列
        saveDBQueues.sendVisitVideoQueue(videoId);
        //增加播放量
        int count=videoDO.getVisitCount();
        videoDO.setVisitCount(count+1);
        RedisUtil.writeJsonWithTTL(RedisContexts.CACHE_VIDEODO,videoId,videoDO, RedisContexts.CACHE_VIDEODO_TTL);
        return Result.success(videoDO);
    }

    @Override
    public Result popular(Integer pageNum,Integer pageSize) {
        List<VideoInfoDTO> list =  RedisUtil.findJsonList(RedisContexts.CACHE,"popular",VideoInfoDTO.class);
        if(list == null || list.isEmpty()){
            list = videoMapper.popular();
            RedisUtil.writeJsonWithTTL(RedisContexts.CACHE,"popular",list, RedisContexts.CACHE_POPULAR_TTL);
        }
        //创建pageBean
        PageBean pageBean = new PageBean<>();
        pageBean.setData(PageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            throw new ArgsInvalidException("分页参数非法");
        }
        return Result.success(pageBean);
    }
}
