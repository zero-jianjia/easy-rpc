package com.zero.easyrpc.example.demo;

import com.zero.easyrpc.client.annotation.RPCService;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class ByeServiceImpl implements ByeService {

    @Override
    @RPCService(responsibilityName="fly100%",serviceName ="TEST.SAYBYE",isVIPService = true,isSupportDegradeService = false)
    public String sayBye(String str) {
        return "bye " + str;
    }

}
