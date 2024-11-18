package west2project.pojo.DO.chat;

import lombok.Data;

import java.util.Date;

@Data
public class SessionDO {
    private Long id;
    private Long userId1;
    private Long userId2;
    private Long groupId;
    private String lastMessage;
    private Date updatedAt;
}
