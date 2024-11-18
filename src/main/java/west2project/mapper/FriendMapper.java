package west2project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import west2project.pojo.DO.chat.FriendDO;

import java.util.Date;
import java.util.List;

@Mapper
public interface FriendMapper {
    @Select("SELECT IF(#{userId} = user_id_1, user_id_2, user_id_1) AS friend_id FROM west2_online_project.friend WHERE #{userId} = user_id_1 OR #{userId} = user_id_2")
    List<Long> selectFriendIdByUserId(Long userId);

    void batchInsert(List<FriendDO> friendDOList);

    void batchDelete(List<FriendDO> friendDOList);


}
