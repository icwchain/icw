package io.icw.v2.model.dto;

import io.icw.core.rpc.model.ApiModel;
import io.icw.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

@ApiModel(name = "交易资产输入信息")
public class CoinFromDto {
    @ApiModelProperty(description = "账户地址")
    private String address;

    @ApiModelProperty(description = "资产的链id")
    private int assetChainId;
    @ApiModelProperty(description = "资产id")
    private int assetId;
    @ApiModelProperty(description = "资产金额")
    private BigInteger amount;
    @ApiModelProperty(description = "资产nonce值")
    private String nonce;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
