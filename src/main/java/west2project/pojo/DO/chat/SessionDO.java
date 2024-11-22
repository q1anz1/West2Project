package west2project.pojo.DO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor// 一定要加，不然mybatis搞死你
@AllArgsConstructor
public class SessionDO {
    private Long id;
    private Long userId1;
    private Long userId2;
    private Long groupId;
    private String lastMessage;
    private Date updatedAt;

    public SessionDO(Long userId1, Long userId2, Long groupId, String message, Date updatedAt) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.groupId = groupId;
        this.lastMessage = message;
        this.updatedAt = updatedAt;
    }
}
