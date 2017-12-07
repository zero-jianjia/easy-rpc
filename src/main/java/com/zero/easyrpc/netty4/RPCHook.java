package com.zero.easyrpc.netty4;

/**
 * RPC的回调钩子，在发送请求和接收请求的时候触发，这样做事增加程序的健壮性和灵活性
 */
public interface RPCHook {
    void doBeforeRequest(final String remoteAddr, final Transporter request);

    void doAfterResponse(final String remoteAddr, final Transporter request, final Transporter response);
}
