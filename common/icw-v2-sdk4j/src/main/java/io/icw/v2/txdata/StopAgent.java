/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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

package io.icw.v2.txdata;


import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.base.data.NulsHash;
import io.icw.core.exception.NulsException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 停止节点信息类
 * Stop Node Information Class
 *
 * @author tag
 * 2018/11/20
 */
public class StopAgent extends BaseNulsData {

    private byte[] address;

    private NulsHash createTxHash;
    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(this.createTxHash.getBytes());

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.createTxHash = byteBuffer.readHash();
    }

    @Override
    public int size() {
        return this.createTxHash.getBytes().length;
    }

    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        if(null!=address){
            addressSet.add(this.address);
        }
        return addressSet;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public NulsHash getCreateTxHash() {
        return createTxHash;
    }

    public void setCreateTxHash(NulsHash createTxHash) {
        this.createTxHash = createTxHash;
    }
}
