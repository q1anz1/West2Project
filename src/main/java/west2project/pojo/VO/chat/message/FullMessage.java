package west2project.pojo.VO.chat.message;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class FullMessage<T> implements Serializable {
    public static final Integer INIT_WS_MSG = 0;
    public static final Integer SYSTEM_MSG = 1;
    public static final Integer CHAT_MSG = 2;
    public static final HashMap<Integer,String> MESSAGE_TYPE_INFO_MAP = new HashMap<>();
    public static final Long SYSTEM_ID = 0L;
    static {
        MESSAGE_TYPE_INFO_MAP.put(0,"websocket通道初始化");
        MESSAGE_TYPE_INFO_MAP.put(1,"系统消息");
        MESSAGE_TYPE_INFO_MAP.put(2,"聊天消息");
    }

    private Integer messageType;
    private String messageTypeInfo;
    private Long senderId;
    private T msg;

    public static <T> FullMessage<T> init(Integer msgType, Long senderId, T msg) {
        FullMessage<T> fullMessage = new FullMessage<>();
        fullMessage.setMessageType(msgType);
        fullMessage.setMessageTypeInfo(MESSAGE_TYPE_INFO_MAP.get(msgType));
        fullMessage.setSenderId(senderId);
        fullMessage.setMsg(msg);
        return fullMessage;
    }
}
