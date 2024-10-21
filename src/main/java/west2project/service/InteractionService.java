package west2project.service;

import west2project.result.Result;

public interface InteractionService {
    Result like(Long videoId, Long commentId, Integer type);

    Result videoLikeList(Long userId, Integer pageNum, Integer pageSize,Integer type);

    Result publishComment(Long videoId, Long commentId, String context);

    Result deleteComment(Long commentId);

    Result commentList(Long videoId, Long commentId, Integer pageSize, Integer pageNum);
}
