package west2project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import west2project.pojo.DO.chat.GroupDO;

import java.util.List;


@Mapper
public interface GroupUserMapper {

    @Select("SELECT group_id FROM west2_online_project.group_user WHERE user_id = #{userId}")
    List<Long> selectGroupIdListByUserId (Long userId);

}
