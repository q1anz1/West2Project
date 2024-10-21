package west2project.service;

import west2project.result.Result;

public interface AdminService {
    Result deleteUser(Long userId);

    Result deleteVideo(Long videoId);
}
