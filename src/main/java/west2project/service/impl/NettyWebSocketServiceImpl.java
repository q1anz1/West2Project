package west2project.service.impl;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import west2project.service.NettyWebSocketService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NettyWebSocketServiceImpl implements NettyWebSocketService {


    @Override
    public void connect(java.nio.channels.Channel channel) {

    }
}
