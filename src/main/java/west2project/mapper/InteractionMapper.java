package west2project.mapper;

import org.apache.ibatis.annotations.*;
import west2project.pojo.DO.videos.CommentDO;
import west2project.pojo.DO.videos.VideoDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface InteractionMapper {

    @Insert("insert into west2_online_project.video_like(video_id, user_id) values (#{videoId},#{userId})")
    void saveVideoLike(Long videoId,Long userId);

    @Select("select video_id from west2_online_project.video_like where user_id=#{userId}")
    List<Long> findVideoLike(Long userId);

    @Select("select id from west2_online_project.video where id=#{videoId}")
    VideoDO findVideoId(Long videoId);

    @Select("select id, user_id, video_url, cover_url, title, description, visit_count, like_count, " +
            "comment_count, created_at, updated_at, deleted_at " +
            "from west2_online_project.video where id =#{videoId}")
    VideoDO findVideoDO(Long videoId);
    @Select("select id, user_id, video_id, parent_id, like_count, child_count, content, created_at, updated_at, deleted_at" +
            " from west2_online_project.comment where id =#{videoId}")
    CommentDO findCommentDO(Long commentId);
    @Insert("insert into west2_online_project.comment_like(comment_id, user_id) values (#{commentId},#{userId})")
    void saveCommentLike(Long commentId,Long userId);

    @Select("select id from west2_online_project.comment_like where user_id=#{userId}")
    List<Long> findCommentLike(Long userId);
    @Delete("delete from west2_online_project.video_like where user_id=#{userId} and video_id=#{videoId}")
    void removeVideoLike(Long videoId,Long userId);
    @Delete("delete from west2_online_project.comment_like where user_id=#{userId} and comment_id=#{commentId}")
    void removeCommentLike(Long commentId,Long userId);

    @Update("update west2_online_project.video set visit_count=#{visitCount},like_count=#{likeCount},comment_count=#{commentCount}" +
            " where id=#{id}")
    void updateVideoVisitCount(Long id,Integer visitCount,Integer likeCount,Integer commentCount);
    @Update("update west2_online_project.comment set like_count=#{likeCount},child_count=#{childCount},deleted_at=#{deletedAt} where id=#{id}")
    void updateCommentVisitCount(Long id,Integer likeCount,Integer childCount,Date deletedAt);

    @Update("update west2_online_project.user set avatar_url=#{avatarUrl} where id=#{id}")
    void updateUserVisitCount(Long id,String avatarUrl);

    @Select("select video_id from west2_online_project.video_like where user_id=#{userId}")
    List<Long> selectVideoLikeList(Long userId);
    @Select("select comment_id from west2_online_project.comment_like where user_id=#{userId}")
    List<Long> selectCommentLikeList(Long userId);
    void saveCommentDO(CommentDO commentDO);
    @Select("select id, user_id, video_id, parent_id, like_count, child_count, content, created_at, updated_at, deleted_at" +
            " from west2_online_project.comment where video_id = #{videoId}")
    List<CommentDO> selectVideoCommentList(Long videoId);
    @Select("select id, user_id, video_id, parent_id, like_count, child_count, content, created_at, updated_at, deleted_at" +
            " from west2_online_project.comment where parent_id = #{parentId}")
    List<CommentDO> selectCommentCommentList(Long parentId);
}
