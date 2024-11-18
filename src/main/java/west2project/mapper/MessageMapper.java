package west2project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface MessageMapper {
    @Insert("INSERT INTO west2_online_project.message(user_id, text, picture_url, to_user_id, group_id, created_at) " +
            "VALUES(#{userId},#{text},#{pictureUrl},#{toUserId},#{groupId},#{createdAt})")
    void saveMessageDO(Long userId, String text, String pictureUrl, Long toUserId, Long groupId, Date createdAt);
}
