package west2project.pojo.DO.users;


import lombok.Data;

import java.util.Date;



@Data
public class UserDO {
    private Long id;
    private String username;
    private String password;
    private String avatarUrl;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private String email;
    private String role;
}
