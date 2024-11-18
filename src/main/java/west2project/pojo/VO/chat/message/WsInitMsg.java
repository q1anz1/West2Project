package west2project.pojo.VO.chat.message;

import lombok.Data;
import west2project.pojo.VO.chat.GroupVO;
import west2project.pojo.VO.chat.SessionVO;
import west2project.pojo.VO.user.UserInfoVO;

import java.io.Serializable;
import java.util.List;

@Data
public class WsInitMsg implements Serializable {
    private List<SessionVO> sessionVOList;
    private List<UserInfoVO> friendUserInfoVOList;
    private List<GroupVO> groupVOList;
}
