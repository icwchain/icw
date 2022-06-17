/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.base.api.provider.ledger.facade;

import io.icw.base.api.provider.BaseReq;

/**
 * @Author: ljs
 * @Time: 2019-08-06 16:50
 * @Description: 功能描述
 */
public class RegLocalAssetReq extends BaseReq {
    private String assetSymbol;
    private String assetName;
    private long initNumber;
    private int decimalPlace;
    private String txCreatorAddress;
    private String assetOwnerAddress;
    private String password = "";

    public RegLocalAssetReq(String address,
                            String symbol, String assetName, long initNumber, int decimalPlace,
                            String password, String assetOwnerAddress) {
        this.txCreatorAddress = address;
        this.assetSymbol = symbol;
        this.assetName = assetName;
        this.initNumber = initNumber;
        this.decimalPlace = decimalPlace;
        this.password = password;
        this.assetOwnerAddress = assetOwnerAddress;
    }

    public String getTxCreatorAddress() {
        return txCreatorAddress;
    }

    public void setTxCreatorAddress(String txCreatorAddress) {
        this.txCreatorAddress = txCreatorAddress;
    }

    public String getAssetOwnerAddress() {
        return assetOwnerAddress;
    }

    public void setAssetOwnerAddress(String assetOwnerAddress) {
        this.assetOwnerAddress = assetOwnerAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public long getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(long initNumber) {
        this.initNumber = initNumber;
    }

    public int getDecimalPlace() {
        return decimalPlace;
    }

    public void setDecimalPlace(int decimalPlace) {
        this.decimalPlace = decimalPlace;
    }
}
