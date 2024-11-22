package west2project.pojo.DO.video;

import lombok.Data;

import java.util.Date;

@Data
public class VideoDO {
    private Long id;
    private Long userId;
    private String videoUrl;
    private String coverUrl;
    private String title;
    private String description;
    private Integer visitCount;
    private Integer likeCount;
    private Integer commentCount;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private Boolean review;
}
