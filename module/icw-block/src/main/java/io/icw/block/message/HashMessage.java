/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.block.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseBusinessMessage;
import io.icw.base.data.NulsHash;
import io.icw.core.exception.NulsException;

import java.io.IOException;

/**
 * 发送一个hash
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:37
 */
public class HashMessage extends BaseBusinessMessage {

    private NulsHash requestHash;

    public HashMessage() {
    }

    public NulsHash getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(NulsHash requestHash) {
        this.requestHash = requestHash;
    }

    public HashMessage(NulsHash hash) {
        this.requestHash = hash;
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(requestHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.requestHash = byteBuffer.readHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HashMessage that = (HashMessage) o;

        return requestHash != null ? requestHash.equals(that.requestHash) : that.requestHash == null;
    }

    @Override
    public int hashCode() {
        return requestHash != null ? requestHash.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HashMessage{" +
                "requestHash=" + requestHash +
                '}';
    }
}
