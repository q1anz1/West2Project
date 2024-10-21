package west2project.pojo.VO.users;


import lombok.Data;

import java.util.Date;

@Data
public class LoginVO {
    private String JwtToken;
    private Long id;
    private String username;
    private String avatarUrl;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private String role;
}
