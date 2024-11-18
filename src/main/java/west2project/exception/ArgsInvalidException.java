package west2project.exception;

import lombok.Getter;

import static west2project.enums.ResponseCodeEnum.CODE_400;

@Getter
public class ArgsInvalidException extends MyRuntimeException{
    public ArgsInvalidException(String message) {
        super(message, CODE_400.getCode());
    }
}
