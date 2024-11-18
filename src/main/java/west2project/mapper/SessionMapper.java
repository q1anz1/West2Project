package west2project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import west2project.pojo.DO.chat.SessionDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface SessionMapper {
    @Select("SELECT id, user_id_1, user_id_2, group_id, last_message, updated_at FROM west2_online_project.session " +
            "WHERE (user_id_1 = #{userId} OR user_id_2 = #{userId}) AND updated_at >= NOW() - INTERVAL 30 DAY " +
            "ORDER BY updated_at DESC")
    List<SessionDO> selectSessionDOListLast30DaysByUserId(Long userId);
}
