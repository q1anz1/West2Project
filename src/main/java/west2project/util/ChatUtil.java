package west2project.util;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import lombok.extern.slf4j.Slf4j;
import west2project.exception.ArgsInvalidException;

import static west2project.enums.ResponseCodeEnum.CODE_400;

@Slf4j
public class ChatUtil {
    private static final Integer MAX_CHAT_TEXT_LENGTH = 999;

    public static void isMsgValid(String text) {
        if (isOutOfLength(text)) {
            throw new ArgsInvalidException("文本字数过多或为空");
        }
        // 敏感词检查
        if (isSensitive(text)) {
            throw new ArgsInvalidException("检测到敏感词");
        }
    }

    private static boolean isOutOfLength(String text) {
        return text.getBytes().length >= MAX_CHAT_TEXT_LENGTH || text.isEmpty();
    }

    private static boolean isSensitive(String text) {
        return SensitiveWordHelper.contains(text);
    }
}
