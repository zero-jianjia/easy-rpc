package com.zero.easyrpc.transport.netty4;

public class ServerConfig {

    private int listenPort = 8888;

    private int workerThreads = 1;

    private int channelInactiveHandlerThreads = 1;

    private int socketSndBufSize = -1;
    private int socketRcvBufSize = -1;

    private int writeBufferLowWaterMark = -1;
    private int writeBufferHighWaterMark = -1;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }


    public int getChannelInactiveHandlerThreads() {
        return channelInactiveHandlerThreads;
    }

    public void setChannelInactiveHandlerThreads(int channelInactiveHandlerThreads) {
        this.channelInactiveHandlerThreads = channelInactiveHandlerThreads;
    }

    public int getSocketSndBufSize() {
        return socketSndBufSize;
    }

    public void setSocketSndBufSize(int socketSndBufSize) {
        this.socketSndBufSize = socketSndBufSize;
    }

    public int getSocketRcvBufSize() {
        return socketRcvBufSize;
    }

    public void setSocketRcvBufSize(int socketRcvBufSize) {
        this.socketRcvBufSize = socketRcvBufSize;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }
}
