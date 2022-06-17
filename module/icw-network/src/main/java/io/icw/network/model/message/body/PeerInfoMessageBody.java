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

package io.icw.network.model.message.body;


import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;


/**
 * 节点信息广播消息
 * local info send message body
 *
 * @author lan
 * @date 2018/11/01
 */
public class PeerInfoMessageBody extends BaseNulsData {

    private long blockHeight = 0;
    private String blockHash = "";

    public PeerInfoMessageBody() {

    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32();
        s += SerializeUtils.sizeOfString(blockHash);
        return s;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(blockHeight);
        stream.writeString(blockHash);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        blockHeight = buffer.readUint32();
        blockHash = buffer.readString();
    }

}
