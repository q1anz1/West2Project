package west2project.pojo.VO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
@Data
@AllArgsConstructor
public class GroupVO {
    private Long id;
    private String groupName;
    private Long leaderId;
    private String text;
    private String avatarUrl;
    private Long Count;
}
