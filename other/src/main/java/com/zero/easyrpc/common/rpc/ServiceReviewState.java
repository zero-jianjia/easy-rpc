package com.zero.easyrpc.common.rpc;

/**
 * 服务审核状态
 * Created by jianjia1 on 17/12/04.
 */
public enum ServiceReviewState {

    HAS_NOT_REVIEWED, //未审核
    PASS_REVIEW,      //通过审核
    NOT_PASS_REVIEW,  //未通过审核
    FORBIDDEN         //禁用

}