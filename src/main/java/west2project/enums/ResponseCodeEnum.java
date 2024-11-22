package west2project.enums;

import lombok.Getter;

@Getter
public enum ResponseCodeEnum {
    CODE_200(200,"OK"),
    CODE_400(400,"Bad request"),
    CODE_404(404,"Page not found"),
    CODE_999(999, "error"),
    CODE_666(666,"too fast");

    private final Integer code;
    private final String info;

    ResponseCodeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
