package west2project.util;

import lombok.extern.slf4j.Slf4j;
import west2project.exception.ArgsInvalidException;

import static west2project.enums.ResponseCodeEnum.CODE_400;

@Slf4j
public class ChatUtil {
    private static final Integer MAX_CHAT_TEXT_LENGTH = 999;

    public static void isMsgValid(String text) {
        if (isOutOfLength(text)) {
            throw new ArgsInvalidException("文本字数过多");
        }
        // TODO 敏感词检查
    }

    private static boolean isOutOfLength(String text) {
        return text.getBytes().length >= MAX_CHAT_TEXT_LENGTH;
    }
}
