package io.icw.base.api.provider.consensus.facade;

import io.icw.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:51
 * @Description:
 * 停止共识
 * stop  consensus
 */
public class StopMultiSignAgentReq extends BaseReq {

    /**
     * 共识地址
     */
    String address;

    String password;

    String signAddress;

    public StopMultiSignAgentReq(String address, String password) {
        this.address = address;
        this.password = password;
    }

    public StopMultiSignAgentReq(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSignAddress() {
        return signAddress;
    }

    public void setSignAddress(String signAddress) {
        this.signAddress = signAddress;
    }
}
