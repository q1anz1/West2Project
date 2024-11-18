package west2project.exception;

import lombok.Getter;

@Getter
public class MyRuntimeException extends RuntimeException{
    private final Integer code;

    public MyRuntimeException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
