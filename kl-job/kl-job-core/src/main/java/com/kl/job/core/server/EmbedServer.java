package com.kl.job.core.server;

import com.kl.job.core.biz.ExecutorBiz;
import com.kl.job.core.biz.client.ExecutorBizClient;
import com.kl.job.core.biz.impl.ExecutorBizImpl;
import com.kl.job.core.util.GsonTool;
import com.kl.job.core.util.XxlJobRemotingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.ThreadPool;

import java.nio.charset.Charset;
import java.util.concurrent.*;

public class EmbedServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);

    private ExecutorBiz executorBiz;
    private Thread thread;

    public void start(final String address, final int port, final String appname,
                      final String accessToken) {
        executorBiz = new ExecutorBizImpl();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                ThreadPoolExecutor bizThreadPool = new ThreadPoolExecutor(
                        0, 200, 60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(2000),
                        new ThreadFactory() {
                            @Override
                            public Thread newThread(Runnable r) {
                                return new Thread(r, "xxl-job, EmbedServer bizThreadPool-" + r.hashCode());
                            }
                        },
                        new RejectedExecutionHandler() {
                            @Override
                            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                throw new RuntimeException("xxl-job, EmbedServer bizThreadPool is EXHAUSTED!");
                            }
                        }

                );

                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup,workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))
                                            .addLast(new HttpServerCodec())
                                            .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                            .addLast(new EmbedHttpServerHandler)
                                }
                            })
                            .childOption();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }
    public static class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private static final Logger logger = LoggerFactory.getLogger(EmbedHttpServerHandler.class);
        private ExecutorBiz executorBiz;
        private String accessToken;
        private ThreadPoolExecutor threadPoolExecutor;

        public EmbedHttpServerHandler(ExecutorBiz executorBiz, String accessToken, ThreadPoolExecutor threadPoolExecutor) {
            this.executorBiz = executorBiz;
            this.accessToken = accessToken;
            this.threadPoolExecutor = threadPoolExecutor;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
            String requestData = msg.content().toString(CharsetUtil.UTF_8);
            String uri = msg.uri();
            HttpMethod httpMethod = msg.getMethod();
            boolean keepAlive = HttpUtil.isKeepAlive(msg);
            String accessTokenReq = msg.headers().get(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN);

            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Object responseObj = process(httpMethod, uri, requestData, accessTokenReq);

                    String responseJson = GsonTool.toJson(responseObj);

                    writeResponse(ctx, keepAlive, responseJson);
                }
            });
        }

        private Object process(HttpMethod httpMethod, String uri, String requestData, String accessTokenReq) {
        }

        private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        }
    }
}
