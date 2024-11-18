package west2project.service;

import west2project.result.Result;

public interface SocializingService {
    Result action(Long toUserId, Integer type);

    Result followList(Long userId, Integer pageNum, Integer pageSize);

    Result fanList(Long userId, Integer pageNum, Integer pageSize);

    Result friendList(Integer pageNum, Integer pageSize);

}
