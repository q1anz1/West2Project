package west2project.exception;

import static west2project.enums.ResponseCodeEnum.CODE_400;

public class UserException extends MyRuntimeException{

    public UserException(String message) {
        super(message, CODE_400.getCode());
    }
}
