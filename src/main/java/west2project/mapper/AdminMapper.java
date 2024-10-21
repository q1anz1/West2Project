package west2project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import west2project.pojo.DO.users.UserDO;
import west2project.pojo.DO.videos.VideoDO;

import java.util.Date;

@Mapper
public interface AdminMapper {
    @Update("update west2_online_project.user set deleted_at=#{date},role='banned' where id = #{userId}")
    void deleteUser(Long userId, Date date);

    @Update("update west2_online_project.video set deleted_at=#{date} where id = #{videoId}")
    void deleteVideo(Long videoId,Date date);

    @Select("select id, user_id, video_url, cover_url, title, description, visit_count, like_count, comment_count, created_at, updated_at, deleted_at " +
            "from west2_online_project.video where id = #{videoId}")
    VideoDO selectVideoDOById(Long videoId);

    @Select("select id, username, password, avatar_url, created_at, updated_at, deleted_at, email, role " +
            "from west2_online_project.user where id = #{userId}")
    UserDO selectUserDOById(Long userId);
}
