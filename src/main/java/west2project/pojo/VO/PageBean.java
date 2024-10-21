package west2project.pojo.VO;

import lombok.Data;
import java.util.List;

@Data
public class PageBean<T> {
    private Long totalPage;
    private List<T> data;


}
