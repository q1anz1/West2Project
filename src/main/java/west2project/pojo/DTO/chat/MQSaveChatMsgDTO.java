package west2project.pojo.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import west2project.pojo.DO.chat.MessageDO;

@Data
@AllArgsConstructor
public class MQSaveChatMsgDTO {
    private MessageDO messageDO;
    private String uuid;
}
