package west2project.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import west2project.exception.ArgsInvalidException;
import west2project.exception.UserException;
import west2project.result.Result;

import static west2project.enums.ResponseCodeEnum.CODE_400;

@RestControllerAdvice
public class GlobalExceptionHandler {

/*    @ExceptionHandler(Exception.class)
    public Result<?> handlerException(Exception e) {
        return Result.error(999, e.getMessage());
    }*/

    @ExceptionHandler(ArgsInvalidException.class)
    public Result<?> handlerArgsInvalidException(ArgsInvalidException e) {
        return Result.error(CODE_400.getCode(), e.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public Result<?> handlerUserException(UserException e) {
        return Result.error(CODE_400.getCode(), e.getMessage());
    }
}
