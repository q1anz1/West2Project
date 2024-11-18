package west2project.pojo.DO.video;

import lombok.Data;

import java.util.Date;

@Data
public class CommentDO {
    private Long id;
    private Long userId;
    private Long videoId;
    private Long parentId;
    private Integer likeCount;
    private Integer childCount;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

    public CommentDO(Long userId,String context) {
        this.userId = userId;
        this.content = context;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
        this.likeCount = 0;
        this.childCount = 0;
    }
}
