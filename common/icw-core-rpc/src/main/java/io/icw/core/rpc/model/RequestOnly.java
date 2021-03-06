package io.icw.core.rpc.model;

import io.icw.core.rpc.model.message.Request;

/**
 * 请求调用远程方法且不需返回
 * Request calls to remote methods without requiring return
 *
 * @author tag
 * @date 2019/09/09
 */

public class RequestOnly {
    private Request request;

    private int messageSize;

    public  RequestOnly(Request request, int messageSize){
        this.request = request;
        this.messageSize = messageSize;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }
}
