package west2project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import west2project.pojo.DO.chat.SessionDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface SessionMapper {
    @Select("SELECT id, user_id_1, user_id_2, group_id, last_message, updated_at FROM west2_online_project.session " +
            "WHERE (user_id_1 = #{userId} OR user_id_2 = #{userId}) AND (updated_at >= NOW() - INTERVAL 30 DAY) " +
            "ORDER BY updated_at DESC")
    List<SessionDO>  selectSessionDOListLast30DaysByUserId(Long userId);

    @Select("SELECT id, user_id_1, user_id_2, group_id, last_message, updated_at FROM west2_online_project.session " +
            "WHERE (user_id_1=#{userId1} AND user_id_2=#{userId2}) OR (user_id_1=#{userId2} AND user_id_2=#{userId1})")
    SessionDO selectSessionDOByUserId1UserId2(Long userId1, Long userId2);
    @Select("SELECT id, user_id_1, user_id_2, group_id, last_message, updated_at FROM west2_online_project.session " +
            "WHERE user_id_1=#{userId} AND group_id=#{groupId}")
    SessionDO selectSessionDOByUserIdGroupId(Long userId, Long groupId);
    void saveSessionDO(SessionDO sessionDO);

    @Select("SELECT id, user_id_1, user_id_2, group_id, last_message, updated_at FROM west2_online_project.session " +
            "WHERE id = #{sessionId}")
    SessionDO selectSessionDOBySessionId(Long sessionId);

    @Update("UPDATE west2_online_project.session SET last_message = #{text}, updated_at = #{updatedAt} WHERE id = #{sessionId}")
    void updateSession(Long sessionId, String text, Date updatedAt);
}
