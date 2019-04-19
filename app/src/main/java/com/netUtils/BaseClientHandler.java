package com.netUtils;

import android.util.Log;

import com.protocol.ServiceProtocalHandler;
import com.utils.Log4;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class BaseClientHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAGS = "BaseClientHandler";
    private ServiceProtocalHandler _ServiceProtocalHandler;

    private static int _TimeCnt = 0;

    public BaseClientHandler(ServiceProtocalHandler mServiceProtocalHandler) {
        this._ServiceProtocalHandler = mServiceProtocalHandler;
    }

    //心跳消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (_TimeCnt == 0) {

                    String str = _ServiceProtocalHandler.heartbeat();
                    Log4.info(str);
                    ctx.channel().writeAndFlush(str);
                }
                if (_TimeCnt++ > 30) {
                    _TimeCnt = 0;
                }
            }
        }
    }

    /**
     * 连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAGS, "channelActive");
        _TimeCnt = 0;
        // NettyClient.getInstance().setConnectStatus(true);
        //listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAGS, "channelInactive");
//        NettyClient.getInstance().setConnectStatus(false);
//        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        // NettyClient.getInstance().reconnect();
    }

    //数据处理
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Log.e(TAGS, "channelRead0");
        if (this._ServiceProtocalHandler != null) {
            _ServiceProtocalHandler.onMessage(msg);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
//        NettyClient.getInstance().setConnectStatus(false);
        Log.e(TAGS, "exceptionCaught");
        //listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }
}
