/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.icw.sdk.core.model.transaction;

import io.icw.sdk.core.contast.TransactionConstant;
import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.model.CancelDeposit;
import io.icw.sdk.core.model.CoinData;
import io.icw.sdk.core.utils.NulsByteBuffer;

/**
 * @author Niels
 */
public class CancelDepositTransaction extends Transaction<CancelDeposit> {

    public CancelDepositTransaction() {
        super(TransactionConstant.TX_TYPE_CANCEL_DEPOSIT);
    }

    public CancelDepositTransaction(CoinData coinData) {
        super(TransactionConstant.TX_TYPE_CANCEL_DEPOSIT);
    }

    @Override
    protected CancelDeposit parseTxData(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new CancelDeposit());
    }

    @Override
    public String getInfo(byte[] address) {

        return "--";
    }

    @Override
    public boolean isUnlockTx() {
        return true;
    }
}