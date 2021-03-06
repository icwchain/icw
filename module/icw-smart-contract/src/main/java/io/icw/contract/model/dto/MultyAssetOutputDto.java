/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.icw.contract.model.dto;

import io.icw.base.basic.AddressTool;
import io.icw.contract.model.bo.MultyAssetOutput;
import io.icw.core.rpc.model.ApiModel;
import io.icw.core.rpc.model.ApiModelProperty;

import static io.icw.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 * @date: 2019-03-06
 */
@ApiModel
public class MultyAssetOutputDto {
    @ApiModelProperty(description = "转入地址")
    private String to;
    @ApiModelProperty(description = "转入金额")
    private String value;
    @ApiModelProperty(description = "转入金额资产链ID")
    private int assetChainId;
    @ApiModelProperty(description = "转入金额资产ID")
    private int assetId;
    @ApiModelProperty(description = "转入金额锁定时间")
    private long lockTime;

    public MultyAssetOutputDto() {
    }

    public MultyAssetOutputDto(MultyAssetOutput output) {
        this.to = AddressTool.getStringAddressByBytes(output.getTo());
        this.value = bigInteger2String(output.getValue());
        this.assetChainId = output.getAssetChainId();
        this.assetId = output.getAssetId();
        this.lockTime = output.getLockTime();
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
