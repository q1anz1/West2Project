package west2project.pojo.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import west2project.pojo.VO.chat.message.ChatMsg;
import west2project.pojo.VO.chat.message.FullMessage;

@Data
@AllArgsConstructor
public class MQFullMessageDTO {
    Boolean toAUser;
    Long targetId;
    FullMessage<ChatMsg> fullMessage;

}
