package west2project.pojo.DTO.videos;

import lombok.Data;

import java.util.Date;

@Data
public class VideoInfoDTO {
    private Long id;
    private Long userId;
    private String coverUrl;
    private String title;
    private Integer visitCount;
    private Date createdAt;
}
