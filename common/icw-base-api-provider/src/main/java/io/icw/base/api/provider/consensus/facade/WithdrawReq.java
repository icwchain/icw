package io.icw.base.api.provider.consensus.facade;

import io.icw.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:57
 * @Description:
 * 退出共识
 */
public class WithdrawReq extends BaseReq {

    String address;

    String txHash;

    String password;

    public WithdrawReq(String address, String txHash, String password) {
        this.address = address;
        this.txHash = txHash;
        this.password = password;
    }

    public WithdrawReq(String address, String txHash) {
        this.address = address;
        this.txHash = txHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}