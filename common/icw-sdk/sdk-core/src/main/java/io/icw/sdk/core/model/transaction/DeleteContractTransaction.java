/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.icw.sdk.core.model.transaction;

import io.icw.sdk.core.contast.ContractConstant;
import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.model.ContractResult;
import io.icw.sdk.core.model.DeleteContractData;
import io.icw.sdk.core.model.Na;
import io.icw.sdk.core.utils.NulsByteBuffer;

/**
 * copy from nuls repository
 *
 * @Author: PierreLuo
 * Created by wangkun23 on 2018/11/8.
 */
public class DeleteContractTransaction extends Transaction<DeleteContractData> implements ContractTransaction {

    private ContractResult contractResult;

    private transient Na returnNa;


    public DeleteContractTransaction() {
        super(ContractConstant.TX_TYPE_DELETE_CONTRACT);
    }

    @Override
    protected DeleteContractData parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new DeleteContractData());
    }

    @Override
    public String getInfo(byte[] address) {
        return "-" + getFee().toCoinString();
    }

    @Override
    public ContractResult getContractResult() {
        return contractResult;
    }

    @Override
    public void setContractResult(ContractResult contractResult) {
        this.contractResult = contractResult;
    }

    @Override
    public void setReturnNa(Na returnNa) {
        this.returnNa = returnNa;
    }

    @Override
    public Na getFee() {
        Na resultFee = super.getFee();
        if (returnNa != null) {
            resultFee = resultFee.minus(returnNa);
        }
        return resultFee;
    }

}
