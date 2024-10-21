package west2project.mapper;

import cn.hutool.core.date.DateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import west2project.pojo.DO.videos.VideoDO;
import west2project.pojo.DTO.videos.VideoInfoDTO;

import java.util.Date;
import java.util.List;

@Mapper
public interface VideoMapper {

    @Insert("insert into west2_online_project.video(user_id, video_url, cover_url, title, description,created_at," +
            " updated_at) values(#{userId},#{videoUrl},#{coverUrl},#{title},#{description},#{createdAt},#{updatedAt})")
    void saveVideo(Long userId, String videoUrl, String coverUrl, String title, String description,
                   DateTime createdAt,DateTime updatedAt);
    @Select("select id from west2_online_project.video where user_id=#{userId}")
    List<Long> findVideoPublishList(Long userId);

    @Select("select id from west2_online_project.video where video_url=#{videoUrl}")
    Long findVideoId(String videoUrl);

    List<VideoInfoDTO> searchVideoId(String keywords, Date fromDate, Date toDate, Long userId,Integer pageSize, Integer pageNum);

    @Select("select id from west2_online_project.user where username=#{username}")
    Long findUserId(String username);

    @Select("select id, user_id, video_url, cover_url, title, description, visit_count, like_count, " +
            "comment_count, created_at, updated_at, deleted_at " +
            "from west2_online_project.video where id =#{videoId}")
    VideoDO findVideoDO(Long videoId);

    @Select("SELECT id, user_id, cover_url, title,visit_count,created_at FROM west2_online_project.video ORDER BY visit_count DESC")
    List<VideoInfoDTO> popular();
}
