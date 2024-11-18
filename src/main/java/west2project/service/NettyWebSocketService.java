package west2project.service;

import java.nio.channels.Channel;

public interface NettyWebSocketService {

    void connect(Channel channel);
}
