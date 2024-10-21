package west2project.result;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Integer totalPage;
    private List<T> result;
}
