package io.icw.ledger.test.cmd;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.base.data.Transaction;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TranList extends BaseNulsData {
    private List<Transaction> txs = new ArrayList<>();

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(txs.size());
        for (Transaction tr : txs) {
            stream.writeNulsData(tr);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int c = byteBuffer.readUint16();
        for (int i = 0; i < c; i++) {
            this.txs.add(byteBuffer.readNulsData(new Transaction()));
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        for (Transaction tx : txs) {
            size += SerializeUtils.sizeOfNulsData(tx);
        }
        return size;
    }
}
