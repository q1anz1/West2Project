package west2project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import west2project.pojo.DO.chat.MessageDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Insert("INSERT INTO west2_online_project.message(user_id, text, picture_url, to_user_id, group_id, created_at) " +
            "VALUES(#{userId},#{text},#{pictureUrl},#{toUserId},#{groupId},#{createdAt})")
    void saveMessageDO(Long userId, String text, String pictureUrl, Long toUserId, Long groupId, Date createdAt);

    @Select("SELECT id, user_id, text, picture_url, to_user_id, group_id, created_at FROM west2_online_project.message " +
            "WHERE ((user_id = #{userId} AND to_user_id = #{toUserId}) OR (user_id = #{toUserId} AND to_user_id = #{userId})) " +
            "AND (created_at >= NOW() - INTERVAL 30 DAY)")
    List<MessageDO> getFriendAndOwnMessageLast30Days(Long userId, Long toUserId);

    @Select("SELECT id, user_id, text, picture_url, to_user_id, group_id, created_at FROM west2_online_project.message " +
            "WHERE (group_id = #{groupId}) AND (created_at >= NOW() - INTERVAL 30 DAY)")
    List<MessageDO> getGroupMessageLast30Days(Long groupId);

    @Select("SELECT id, user_id, text, picture_url, to_user_id, group_id, created_at FROM west2_online_project.message " +
            "WHERE (created_at >= #{date}) AND (to_user_id=#{userId})")
    List<MessageDO> getUnreadFriendMessage(Long userId, Date date);

    @Select("SELECT id, user_id, text, picture_url, to_user_id, group_id, created_at FROM west2_online_project.message " +
            "WHERE created_at >= #{date} " +
            "AND group_id IN (SELECT group_id FROM west2_online_project.group_user WHERE user_id=#{userId})")
    List<MessageDO> getUnreadGroupMessage(Long userId, Date date);
}
