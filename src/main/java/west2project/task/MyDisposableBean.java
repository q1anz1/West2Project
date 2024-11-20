package west2project.task;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MyDisposableBean implements DisposableBean {
    private final SaveDBTask saveDBTask;
    @Override
    public void destroy() {

    }
}
