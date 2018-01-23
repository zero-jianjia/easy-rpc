
package com.zero.easyrpc.rpc.protocol;


import com.zero.easyrpc.rpc.Caller;
import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;

/**
 * 
 * filter before transport.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-16
 */
public interface Filter {

    Response filter(Caller<?> caller, Request request);
}
