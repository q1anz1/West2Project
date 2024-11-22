package west2project.mapper;

import cn.hutool.core.date.DateTime;
import org.apache.ibatis.annotations.*;
import west2project.pojo.DO.video.VideoDO;
import west2project.pojo.DTO.video.VideoInfoDTO;

import java.util.Date;
import java.util.List;

@Mapper
public interface VideoMapper {

    @Insert("insert into west2_online_project.video(user_id, video_url, cover_url, title, description,created_at," +
            " updated_at) values(#{userId},#{videoUrl},#{coverUrl},#{title},#{description},#{createdAt},#{updatedAt})")
    void saveVideo(Long userId, String videoUrl, String coverUrl, String title, String description,
                   DateTime createdAt,DateTime updatedAt);
    @Select("select id from west2_online_project.video where user_id=#{userId}")
    List<Long> findVideoPublishListByUserId(Long userId);

    @Select("select id from west2_online_project.video where video_url=#{videoUrl}")
    Long findVideoIdByVideoUrl(String videoUrl);

    List<VideoInfoDTO> searchVideoId(String keywords, Date fromDate, Date toDate, Long userId,Integer pageSize, Integer pageNum);

    @Select("select id from west2_online_project.user where username=#{username}")
    Long findUserIdByUsername(String username);

    @Select("select id, user_id, video_url, cover_url, title, description, visit_count, like_count, " +
            "comment_count, created_at, updated_at, deleted_at, review " +
            "from west2_online_project.video where id =#{videoId}")
    VideoDO findVideoDOByVideoId(Long videoId);

    @Select("SELECT id, user_id, cover_url, title, visit_count,created_at FROM west2_online_project.video ORDER BY visit_count DESC")
    List<VideoInfoDTO> popular();

    @Select("SELECT id, user_id, cover_url, title, visit_count, created_at FROM west2_online_project.video WHERE review = false " +
            "AND deleted_at IS NULL")
    List<VideoInfoDTO> selectReviewVideoList();

    @Update("UPDATE west2_online_project.video SET review = true WHERE id = #{videoId}")
    void updateReviewToTrue(Long videoId);

    @Update("UPDATE west2_online_project.video SET deleted_at = #{deletedAt} WHERE id = #{videoId}")
    void deleteVideoByVideoId(Long videoId, Date deletedAt);
}
