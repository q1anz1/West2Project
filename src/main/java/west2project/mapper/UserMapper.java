package west2project.mapper;

import cn.hutool.core.date.DateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import west2project.pojo.DO.user.UserDO;
import west2project.pojo.VO.user.UserInfoVO;

import java.util.Date;

@Mapper
public interface UserMapper {
    @Insert("insert into west2_online_project.user(username, password, avatar_url, created_at, updated_at,email) " +
            "values(#{username},#{password},#{avatarUrl},#{createAt},#{updateAt},#{email})")
    void saveUser(String username, String password, String avatarUrl, Date createAt, Date updateAt, String email);

    @Select("select id,username,password,avatar_url,created_at,updated_at,deleted_at,email,role from west2_online_project.user " +
            "where username=#{username}")
    UserDO findUserByUsername(String username);

    @Select("select id from west2_online_project.user where username=#{username}")
    Long findUserIdByUsername(String username);

    @Select("select id from west2_online_project.user where email=#{email}")
    String findUserIdByEmail(String email);

    @Select("select id,username,avatar_url,created_at,updated_at,deleted_at,role from west2_online_project.user " +
            "where id=#{userId}")
    UserInfoVO findUserInfoVOByUserId(Long userId);

    @Update("update west2_online_project.user set avatar_url=#{path},updated_at=#{updatedAt} where id=#{id}")
    void saveAvatar(Long id, String path, DateTime updatedAt);

    @Update("UPDATE west2_online_project.user SET updated_at=#{date} WHERE id=#{userID}")
    void updateLastOnlineTime(Long userId, Date date);

    @Select("SELECT updated_at FROM west2_online_project.user WHERE id=#{userId}")
    Date selectUpdatedAtByUserId(Long userId);
}
