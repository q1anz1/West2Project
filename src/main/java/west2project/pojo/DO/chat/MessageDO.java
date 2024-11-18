package west2project.pojo.DO.chat;

import lombok.Data;

import java.util.Date;

@Data
public class MessageDO {
    private Long id;
    private Long userId;
    private String text;
    private String pictureUrl;
    private Long toUserId;
    private Long groupId;
    private Date createdAt;

    public MessageDO(Long userId, String text, String pictureUrl, Long toUserId, Long groupId, Date createdAt) {
        this.userId = userId;
        this.text = text;
        this.pictureUrl = pictureUrl;
        this.toUserId = toUserId;
        this.groupId = groupId;
        this.createdAt = createdAt;
    }
}
