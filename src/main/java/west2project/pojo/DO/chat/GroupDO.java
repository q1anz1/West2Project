package west2project.pojo.DO.chat;

import lombok.Data;

@Data
public class GroupDO {
    private Long id;
    private String groupName;
    private Long leaderId;
    private String text;
    private String avatarUrl;
    private Long Count;
}
