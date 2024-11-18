package west2project.pojo.VO.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class ChatMsg implements Serializable {
    private String text;
    private String pictureUrl;
    private Date createdAt;
}
