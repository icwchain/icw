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

package io.icw.base.data;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.core.constant.ErrorCode;
import io.icw.core.constant.ToolsConstant;
import io.icw.core.exception.NulsException;
import io.icw.core.exception.NulsRuntimeException;

import java.io.IOException;

/**
 * 通用消息体
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午10:44
 */
public abstract class BaseBusinessMessage extends BaseNulsData {

    private transient NulsHash msgHash;

    @Override
    public int size() {
        return 4;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(ToolsConstant.PLACE_HOLDER);

    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        buffer.readBytes(4);
    }

    public NulsHash getMsgHash() {
        if (msgHash == null) {
            try {
                this.msgHash = NulsHash.calcHash(this.serialize());
            } catch (IOException e) {
                throw new NulsRuntimeException(ErrorCode.init("10003"));
            }
        }
        return msgHash;
    }

}
