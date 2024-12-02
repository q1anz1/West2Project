package west2project.exception;

import lombok.Getter;

import static west2project.enums.ResponseCodeEnum.CODE_400;
@Getter
public class WebSocketArgsInvalidException extends MyRuntimeException{
    public WebSocketArgsInvalidException(String message) {
        super(message, CODE_400.getCode());
    }
}
