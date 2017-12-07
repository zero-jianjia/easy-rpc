package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.RequestProcessor;
import com.zero.easyrpc.netty4.model.RemotingResponse;
import com.zero.easyrpc.netty4.util.ConnectionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
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
 * 该抽象类则取完成使用子类创建好的channel去与远程端交互
 * Created by jianjia1 on 17/12/04.
 */
public class BaseServer {

    private static final Logger logger = LoggerFactory.getLogger(BaseServer.class);

    /**
     * key为请求的id  value是远程返回的结果
     */
    private final Map<Long, RemotingResponse> responseMap = new ConcurrentHashMap<>(256);

    //如果 没有注入针对某个特定请求类型使用特定的处理器 的时候，默认使用该默认的处理器
    protected Pair<RequestProcessor, ExecutorService> defaultRequestProcessor;

    protected Pair<ChannelInactiveProcessor, ExecutorService> defaultChannelInactiveProcessor;

    protected final ExecutorService defaultExecutor = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "defaultExecutor-" + count.incrementAndGet());
        }
    });

    //注入的某个sign对应的处理器放入到HashMap中，键值对一一匹配
    protected final Map<Byte, Pair<RequestProcessor, ExecutorService>> processorMap = new HashMap<>(64);

    protected RPCHook rpcHook;

    //调用的具体实现
    public Transporter invokeSyncImpl(final Channel channel, final Transporter request, final long timeoutMillis)
            throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException {

        try {
            //构造一个请求的封装体
            final RemotingResponse response = new RemotingResponse(request.getRequestId(), timeoutMillis, null);
            responseMap.put(request.getRequestId(), response);

            //发送请求
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        //如果发送对象成功，则设置成功
                        response.setSendRequestOK(true);
                        return;
                    } else {
                        response.setSendRequestOK(false);
                    }
                    //如果请求发送直接失败，则默认将其从responseMap中移除
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

    protected void processRequest(final ChannelHandlerContext ctx, final Transporter transporter) {

        final Pair<RequestProcessor, ExecutorService> matchedPair = processorMap.get(transporter.getSign());
        final Pair<RequestProcessor, ExecutorService> pair = matchedPair == null ? defaultRequestProcessor : matchedPair;

        if (pair != null) {
            try {
                pair.getValue().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (rpcHook != null) {
                                rpcHook.doBeforeRequest(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), transporter);
                            }
                            final Transporter response = pair.getKey().processRequest(ctx, transporter);
                            if (rpcHook != null) {
                                rpcHook.doAfterResponse(ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), transporter, response);
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
        final RemotingResponse response = responseMap.get(transporter.getRequestId());
        if (response != null) {
            //首先先设值，这样会在countdownlatch wait之前把值赋上
            response.setTransporter(transporter);
            //可以直接countdown
            response.putResponse(transporter);

            responseMap.remove(transporter.getRequestId());
        } else {
            logger.warn("received response but matched Id is removed from responseMap maybe timeout");
            logger.warn(transporter.toString());
        }
    }
}
