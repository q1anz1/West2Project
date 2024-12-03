package west2project.pojo.VO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import west2project.pojo.DO.chat.MessageDO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnreadMessageVO {
    private List<MessageDO> friendMessage;
    private List<MessageDO> groupMessage;
}
