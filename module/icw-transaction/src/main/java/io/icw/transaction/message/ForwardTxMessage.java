package io.icw.transaction.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseBusinessMessage;
import io.icw.base.data.NulsHash;
import io.icw.core.exception.NulsException;

import java.io.IOException;

/**
 * 处理完来自其他节点的交易时，再转发出去
 * 转发来自其他节点的交易
 * 转发交易hash
 * @author: Charlie
 * @date: 2019/04/17
 */
public class ForwardTxMessage extends BaseBusinessMessage {
    /**
     * 交易hash
     */
    private NulsHash txHash;

    public NulsHash getTxHash() {
        return txHash;
    }

    public void setTxHash(NulsHash txHash) {
        this.txHash = txHash;
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(txHash.getBytes());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.txHash = byteBuffer.readHash();
    }
}
