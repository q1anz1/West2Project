package west2project.service.videoServiceImpl;

import cn.hutool.core.date.DateTime;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import west2project.context.Contexts;
import west2project.mapper.VideoMapper;
import west2project.pojo.DO.videos.VideoDO;
import west2project.pojo.DTO.videos.VideoInfoDTO;
import west2project.pojo.VO.PageBean;
import west2project.result.Result;
import west2project.service.VideoService;
import west2project.utils.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private final VideoMapper videoMapper;
    private final TikaUtil tikaUtil;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest httpServletRequest;
    private final QianzUtil qianzUtil;
    private final RedisUtil redisUtil;
    private final PageUtil pageUtil;

    @Override
    public Result publish(MultipartFile video, String title, MultipartFile image, String description) {
        //验证视频合法性并修正
        String isVideoValid = tikaUtil.isVideoValid(video, title);
        if (!isVideoValid.equals("true")) {
            return Result.error(isVideoValid);
        }
        if (title.length() > 20) {
            return Result.error("标题字数过多");
        }
        if (description == null || description.isEmpty()) {
            description = "-";
        }
        if (description.length() > 200) {
            return Result.error("描述字数过多");
        }
        //验证封面合法性
        String isImageValid = tikaUtil.isImageValid(image);
        if (!isImageValid.equals("true")) {
            return Result.error(isImageValid);
        }
        //存入本地
        String videoUrl = qianzUtil.saveFileWithName(video, Contexts.VIDEO_BOX, qianzUtil.changeFileName(video));
        String imageUrl = qianzUtil.saveFileWithName(image, Contexts.COVER_BOX, qianzUtil.changeFileName(image));
        //获取用户id
        Long userId = jwtUtil.getUserId(jwtUtil.getJwt(httpServletRequest));
        //存入数据库
        videoMapper.saveVideo(userId, videoUrl, imageUrl, title, description, DateTime.now(), DateTime.now());
        //将上传信息存入redis
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_PUBLISH_VIDEO_LIST, userId, List.class, videoMapper::findVideoPublishList,
                Contexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        List<Long> list = (List<Long>) result.getData();
        list.add(videoMapper.findVideoId(videoUrl));
        redisUtil.writeJsonWithTTL(Contexts.CACHE_PUBLISH_VIDEO_LIST, userId.toString(), list, Contexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        return Result.success();
    }

    @Override
    public Result videoPublishList(Long userId, Integer pageNum, Integer pageSize) {
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_PUBLISH_VIDEO_LIST, userId, List.class, videoMapper::findVideoPublishList,
                Contexts.CACHE_PUBLISH_VIDEO_LIST_TTL);
        List<Long> list = (List<Long>) result.getData();
        if (list.isEmpty()) {
            return Result.error("无视频");
        }
        List<Long> pageBean = pageUtil.page(list, pageSize, pageNum);
        if (pageBean == null) {
            return Result.error("分页参数非法");
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
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_USERNAME_TO_USERID, username, Long.class,
                videoMapper::findUserId, Contexts.CACHE_USERNAME_TO_USERID_TTL);
        Long userId = (Long) result.getData();
        //在数据库进行查询
        PageHelper.startPage(pageNum, pageSize);
        Page<VideoInfoDTO> p = (Page<VideoInfoDTO>) videoMapper.searchVideoId(keywords, pageSize, pageNum, date1, date2, userId);
        //创建pageBean
        PageBean pageBean = new PageBean<>();
        pageBean.setTotalPage(p.getTotal());
        pageBean.setData(p.getResult());
        if (pageBean.getData() == null || pageBean.getData().isEmpty()) {
            return Result.error("无结果");
        }
        return Result.success(pageBean);
    }

    @Override
    public Result videoFeed(Long videoId) {
        Result result = redisUtil.findJsonWithCache(Contexts.CACHE_VIDEODO,videoId, VideoDO.class,videoMapper::findVideoDO,
                Contexts.CACHE_VIDEODO_TTL);
        VideoDO videoDO = (VideoDO) result.getData();
        if(videoDO==null){
            return Result.error("视频不存在");
        }
        if(videoDO.getDeletedAt() != null){
            return Result.error("视频不见了");
        }
        //加入 有人看过队列
        redisUtil.rightPushList(Contexts.TASK,"visitVideo",videoId);
        //增加播放量
        int count=videoDO.getVisitCount();
        videoDO.setVisitCount(count+1);
        redisUtil.writeJsonWithTTL(Contexts.CACHE_VIDEODO,videoId,videoDO, Contexts.CACHE_VIDEODO_TTL);
        return Result.success(videoDO);
    }

    @Override
    public Result popular(Integer pageNum,Integer pageSize) {
        List<VideoInfoDTO> list =  redisUtil.findJsonList(Contexts.CACHE,"popular",VideoInfoDTO.class);
        if(list == null || list.isEmpty()){
            list = videoMapper.popular();
            redisUtil.writeJsonWithTTL(Contexts.CACHE,"popular",list,Contexts.CACHE_POPULAR_TTL);
        }
        //创建pageBean
        PageBean pageBean = new PageBean<>();
        pageBean.setData(pageUtil.page(list,pageSize,pageNum));
        pageBean.setTotalPage((long) pageBean.getData().size());
        if(pageBean.getData()==null){
            return Result.error("分页参数非法");
        }
        return Result.success(pageBean);
    }
}
