package west2project.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SocializingMapper {

    @Select("select id from west2_online_project.follow where up_id=#{upId} and follower_id=#{followerId}")
    Integer selectFollow(Long upId,Long followerId);

    @Insert("insert into west2_online_project.follow(up_id, follower_id) values(#{upId},#{followerId}) ")
    void saveFollow(Long upId,Long followerId);

    @Delete("delete from west2_online_project.follow where up_id=#{upId} and follower_id=#{followerId}")
    void removeFollow(Long upId,Long followerId);

    @Select("select up_id from west2_online_project.follow where follower_id=#{followerId}")
    List<Long> selectUpId(Long followerId);

    @Select("select follower_id from west2_online_project.follow where up_id=#{upId}")
    List<Long> selectFanId(Long upId);

    @Select("select f2.follower_id " +
            "from west2_online_project.follow f1 " +
            "join west2_online_project.follow f2 on f1.follower_id=f2.up_id and f1.up_id=f2.follower_id " +
            "where f1.follower_id=#{userId}")
    List<Long> selectFriendId(Long userId);

}
