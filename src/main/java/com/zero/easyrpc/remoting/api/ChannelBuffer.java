package com.zero.easyrpc.remoting.api;

public interface ChannelBuffer {

    int readerIndex();

    void readerIndex(int readerIndex);

    boolean readable();

}
