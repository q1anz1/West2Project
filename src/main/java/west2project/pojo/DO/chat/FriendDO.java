package west2project.pojo.DO.chat;

import lombok.Data;

@Data
public class FriendDO {
    private Long id;
    private Long userId1;
    private Long userId2;

    public FriendDO(Long userId1, Long userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
    }
}
