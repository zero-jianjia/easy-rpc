package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.utils.NativeSupport;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.model.Response;
import com.zero.easyrpc.netty4.util.ConnectionUtils;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * netty C/S 端的客户端抽象提取，子类去完成netty的一些创建的事情，
 * 完成使用子类创建好的channel去与远程端交互
 * Created by jianjia1 on 17/12/04.
 */
public class BaseServer {
    private static final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    //存放 sign对应的处理器
    protected final Map<Byte, Pair<Processor, ExecutorService>> processorMap = new HashMap<>(64);

    //如果 没有为sign指定处理器，使用该默认的处理器
    protected Pair<Processor, ExecutorService> defaultRequestProcessor;

    protected Pair<ChannelInactiveProcessor, ExecutorService> defaultChannelInactiveProcessor;

    protected InvokeHook invokeHook;

    // 存放返回结果，<requestId, Response>
    private final Map<Long, Response> responseMap = new ConcurrentHashMap<>(256);

    protected final ExecutorService defaultExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "defaultExecutor-" + count.incrementAndGet());
        }
    });

    //远程调用的具体实现
    public Transporter invokeSyncImpl(final Channel channel, final Transporter request, final long timeoutMillis)
            throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException {

        try {
            //构造一个请求的封装体
            final Response response = new Response(request.getRequestId(), timeoutMillis, null);
            responseMap.put(request.getRequestId(), response);

            //发送请求
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        //如果发送对象成功，则设置成功
                        response.setSendRequestOK(true);
                        return;
                    }
                    //果请求发送直接失败
                    response.setSendRequestOK(false);
                    //将其从responseMap中移除
                    responseMap.remove(request.getRequestId());
                    response.setCause(future.cause()); //记录失败的异常信息
                    response.putResponse(null); //设置当前请求的返回主体返回体是null

                    logger.warn("use channel [{}] send msg [{}] failed and failed reason is [{}]", channel, request, future.cause().getMessage());
                }
            });

            //获取返回结果
            Transporter transporter = response.waitResponse();
            if (transporter == null) {
                if (response.isSendRequestOK()) {//如果发送是成功的，则说明远程端处理超时了
                    throw new RemotingTimeoutException(ConnectionUtils.parseChannelRemoteAddr(channel), timeoutMillis, response.getCause());
                } else {
                    throw new RemotingSendRequestException(ConnectionUtils.parseChannelRemoteAddr(channel), response.getCause());
                }
            }
            return transporter;
        } finally {
            responseMap.remove(request.getRequestId());
        }
    }

    //处理接受到的Transporter
    protected void processMessageReceived(ChannelHandlerContext ctx, Transporter msg) {
        if (logger.isDebugEnabled()) {
            logger.debug("channel [] received Transporter is [{}]", ctx.channel(), msg);
        }

        final Transporter transporter = msg;

        if (transporter != null) {
            switch (transporter.getType()) {
                case Protocol.REQUEST:
                    processRequest(ctx, transporter);
                    break;
                case Protocol.RESPONSE:
                    processResponse(ctx, transporter);
                    break;
                default:
                    break;
            }
        }
    }

    protected void processRequest(final ChannelHandlerContext ctx, final Transporter transporter) {

        final Pair<Processor, ExecutorService> matchedProcessor = processorMap.get(transporter.getSign());
        final Pair<Processor, ExecutorService> finalProcessor = matchedProcessor == null ? defaultRequestProcessor : matchedProcessor;

        if (finalProcessor != null) {
            try {
                finalProcessor.getValue().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (invokeHook != null) {
                                invokeHook.doBeforeRequest(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), transporter);
                            }
                            final Transporter response = finalProcessor.getKey().processRequest(ctx, transporter);
                            if (invokeHook != null) {
                                invokeHook.doAfterResponse(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), transporter, response);
                            }
                            if (response != null) {
                                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture future) throws Exception {
                                        if (!future.isSuccess()) {
                                            logger.error("fail send response ,exception is [{}]", future.cause().getMessage());
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            logger.error("processor occur exception [{}]", e.getMessage());
                            final Transporter response = Transporter.newInstance(transporter.getRequestId(), Protocol.RESPONSE, Protocol.HANDLER_ERROR, null);
                            ctx.writeAndFlush(response);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("server is busy,[{}]", e.getMessage());
                final Transporter response = Transporter.newInstance(transporter.getRequestId(), Protocol.RESPONSE, Protocol.HANDLER_BUSY, null);
                ctx.writeAndFlush(response);
            }
        }
    }

    protected void processResponse(ChannelHandlerContext ctx, Transporter transporter) {
        final Response response = responseMap.get(transporter.getRequestId());
        if (response != null) {
            //首先先设值，这样可以在countdownlatch wait之前把值赋上
            response.setTransporter(transporter);
            //可以直接countdown
            response.putResponse(transporter);

            responseMap.remove(transporter.getRequestId());
        } else {
            logger.warn("received response but matched Id is removed from responseMap maybe timeout");
            logger.warn(transporter.toString());
        }
    }



    protected void processChannelInactive(final ChannelHandlerContext ctx) {
        final Pair<ChannelInactiveProcessor, ExecutorService> pair = this.defaultChannelInactiveProcessor;
        if (pair != null) {
            try {
                pair.getValue().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pair.getKey().processChannelInactive(ctx);
                        } catch (RemotingSendRequestException | RemotingTimeoutException | InterruptedException e) {
                            logger.error("server occor exception [{}]", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("server is busy,[{}]", e.getMessage());
            }
        }
    }

    EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
        return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
    }

    boolean isNativeEt() {
        return NativeSupport.isSupportNativeET();
    }

}
