package west2project.pojo.VO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
public class SessionVO {
    private Long id;
    private Long userId;
    private Long groupId;
    private String name;
    private String avatarUrl;
    private String lastMessage;
    private Date updatedAt;
}
