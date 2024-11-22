package west2project.pojo.DO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDO {
    private Long id;
    private String groupName;
    private Long leaderId;
    private String text;
    private String avatarUrl;
    private Long count;

    public GroupDO(String groupName, Long leaderId, String text, String avatarUrl) {
        this.groupName = groupName;
        this.leaderId = leaderId;
        this.text = text;
        this.avatarUrl = avatarUrl;
    }
}
