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
package io.icw.network.model.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.core.exception.NulsException;
import io.icw.network.model.message.base.BaseMessage;
import io.icw.network.model.message.body.PingPongMessageBody;
import io.icw.network.constant.NetworkConstant;

/**
 * 请求 时间协议消息
 * get time message
 *
 * @author lan
 * @date 2018/11/01
 */
public class PingMessage extends BaseMessage<PingPongMessageBody> {

    @Override
    protected PingPongMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return byteBuffer.readNulsData(new PingPongMessageBody());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public PingMessage() {
        super(NetworkConstant.CMD_MESSAGE_PING);
    }

    public PingMessage(long magicNumber, String cmd, PingPongMessageBody body) {
        super(cmd, magicNumber);
        this.setMsgBody(body);
    }
}