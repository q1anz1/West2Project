package west2project.websocket;


import cn.hutool.extra.spring.SpringUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import west2project.result.Result;
import west2project.util.ChannelUtil;
import west2project.util.JwtUtil;

import java.util.Objects;

@Slf4j
@ChannelHandler.Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ChannelUtil channelUtil;
    private ChannelRead channelRead;

    public NettyWebSocketServerHandler() {
    }

    // 在管道连接时，注入bean，由于不是spring创建的，自动注入用不了，只能手动注
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.channelUtil = SpringUtil.getBean(ChannelUtil.class);
        this.channelRead = SpringUtil.getBean(ChannelRead.class);
    }

    // channel连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    // channel断开
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除在线用户并关闭
        channelUtil.removeChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
        channelRead.handle(ctx,textWebSocketFrame);
    }

    // 心跳和验证
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (Objects.requireNonNull(event.state()) == IdleState.READER_IDLE) {
                // 处理读空闲事件，断开连接并且去除在线用户
                log.info("{}的心跳消失，┏┛墓┗┓...(((m-__-)m",ctx.channel());
                channelUtil.removeChannel(ctx.channel());
            }
        } else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {// 协议升级
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            // 验证权限
            if (token == null || !JwtUtil.verifyToken(token)){
                ctx.channel().close();
                return;
            }
            // 如果通过，初始化Channel
            channelUtil.initChannel(JwtUtil.getUserId(token), ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }

    // 处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        log.warn("webSocket异常发生: ", cause);
        ctx.writeAndFlush(new TextWebSocketFrame(Result.error(400, cause.getMessage()).asJsonString()));
    }

    // 从url中获得token
    private String getToken(String url) {
        if(url == null || !url.contains("?")){
            return null;
        }
        String[] queryParams = url.split("\\?");
        if(queryParams.length != 2){
            return null;
        }
        String[] tokenParams = url.split("=");
        if (tokenParams.length !=2){
            return null;
        }
        return tokenParams[1];
    }
}
