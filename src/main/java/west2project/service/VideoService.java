package west2project.service;

import org.springframework.web.multipart.MultipartFile;
import west2project.result.Result;

import java.util.Date;

public interface VideoService {
    Result publish(MultipartFile video,String title,MultipartFile image,String description);

    Result videoPublishList(Long userId, Integer pageNum, Integer pageSize);

    Result searchVideo(String keywords, Integer pageSize, Integer pageNum,Integer fromDate,Integer toDate, String username);

    Result videoFeed(Long videoId);

    Result popular(Integer pageNum,Integer pageSize);
}
