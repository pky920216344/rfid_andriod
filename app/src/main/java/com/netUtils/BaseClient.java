package com.netUtils;

import android.os.SystemClock;
import android.util.Log;
import com.protocol.ServiceProtocalHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class BaseClient {

    private static final String TAGS = "BaseClient";

    //服务器地址
    private String _host;
    //服务器端口号
    private int _port;
    //重连次数
    // private static int _reConnectCnt = Integer.MAX_VALUE;
    //重连发起间隔 5s
    private long _reConnectIntervalTime = 5000;

    private boolean isConnect = false;
    private boolean isConnecting = false;
    private boolean isNeedReconnect = true;

    private EventLoopGroup _eventLoopGroup;
    private Bootstrap _Bootstrap;
    private Channel _Channel;
    private ServiceProtocalHandler _ServiceProtocalHandlerCallback;

    private void doConnect() {
        synchronized (BaseClient.this) {
            ChannelFuture mChannelFuture = null;
            if (!isConnect) {
                isConnecting = true;
                _eventLoopGroup = new NioEventLoopGroup();
                _Bootstrap = new Bootstrap();
                _Bootstrap.group(_eventLoopGroup).option(ChannelOption.SO_KEEPALIVE, true)
                        //.option(ChannelOption.TCP_NODELAY,true)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        // .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast("ping", new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                //ch.pipeline().addLast(new JsonObjectDecoder());
                                //ch.pipeline().addLast(new LineBasedFrameDecoder(512));//黏包处理
                                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                                ch.pipeline().addLast(new BaseClientHandler(_ServiceProtocalHandlerCallback));
                            }
                        });
                try {
                    mChannelFuture = _Bootstrap.connect(_host, _port).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                Log.e(TAGS, "连接成功");
                                isConnect = true;
                                _Channel = future.channel();
                            } else {
                                Log.e(TAGS, "连接失败");
                                isConnect = false;
                            }

                            isConnecting = false;
                        }
                    }).sync();

                    //Wait until the connection is closed.
                    mChannelFuture.channel().closeFuture().sync();
                    Log.e(TAGS, "连接断开");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isConnect = false;
                    if (null != mChannelFuture) {
                        if (mChannelFuture.channel() != null && mChannelFuture.channel().isOpen()) {
                            mChannelFuture.channel().close();
                        }
                    }
                    _eventLoopGroup.shutdownGracefully();
                    doReConnect();
                }
            }
        }
    }

    private void doReConnect() {
        Log.e(TAGS, "重新连接");
        if (isNeedReconnect && !isConnect) {
            SystemClock.sleep(_reConnectIntervalTime);
            if (isNeedReconnect && !isConnect) {
                Log.e(TAGS, "重新连接");
                doConnect();
            }
        }
    }

    public BaseClient(String host, int port) {
        this._host = host;
        this._port = port;
    }

    public void setCallbackHandler(ServiceProtocalHandler callback) {
        this._ServiceProtocalHandlerCallback = callback;
        this._ServiceProtocalHandlerCallback.setBaseClient(this);
    }

    public void connect() {
        if (isConnecting) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                isNeedReconnect = true;
                doConnect();
            }
        }).start();
    }

    public void disconnect() {
        Log.e(TAGS, "disconnect");
        isNeedReconnect = false;
        _eventLoopGroup.shutdownGracefully();
    }

    public boolean sendMsgToServer(String str) {
        boolean flag = (_Channel != null) && isConnect;

        if (flag) {
            ByteBuf buf = Unpooled.copiedBuffer(str.getBytes());
            _Channel.writeAndFlush(buf);
        }

        return flag;
    }
}
