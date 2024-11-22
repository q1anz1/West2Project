package west2project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface GroupUserMapper {

    @Select("SELECT group_id FROM west2_online_project.group_user WHERE user_id = #{userId}")
    List<Long> selectGroupIdListByUserId(Long userId);

    @Select("SELECT id, group_id, user_id, role FROM west2_online_project.group_user " +
            "WHERE group_id = #{groupId}")
    List<Long> selectGroupUserIdByGroupId(Long groupId);

    @Insert("INSERT INTO west2_online_project.group_user(group_id, user_id, role) " +
            "VALUES(#{groupId},#{userId},#{role})")
    void insertGroupUser(Long userId, Long groupId, Integer role);
}
