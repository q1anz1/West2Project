package west2project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import west2project.pojo.DO.chat.GroupDO;

import java.util.List;

@Mapper
public interface GroupMapper {
    @Select("SELECT id, group_name, leader_id, text, avatar_url, count FROM west2_online_project.`group` WHERE id = #{groupId}")
    GroupDO selectGroupDOByGroupId(Long groupId);

    @Select("SELECT id, group_name, leader_id, text, avatar_url, count FROM west2_online_project.`group` " +
            "WHERE id = (SELECT group_id FROM west2_online_project.group_user where user_id = #{userId})")
    List<GroupDO> selectGroupDOListByUserId(Long userId);
}
