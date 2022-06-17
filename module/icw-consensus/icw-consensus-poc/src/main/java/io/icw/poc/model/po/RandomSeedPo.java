package io.icw.poc.model.po;


import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.core.exception.NulsException;

import java.io.IOException;

/**
 * @author Niels
 */
public class RandomSeedPo extends BaseNulsData {
    private long height;

    private long preHeight;

    private byte[] seed;

    private byte[] nextSeedHash;

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public byte[] getNextSeedHash() {
        return nextSeedHash;
    }

    public void setNextSeedHash(byte[] nextSeedHash) {
        this.nextSeedHash = nextSeedHash;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(preHeight);
        stream.write(seed);
        stream.write(nextSeedHash);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.preHeight = byteBuffer.readInt64();
        this.seed = byteBuffer.readBytes(32);
        this.nextSeedHash = byteBuffer.readBytes(8);
    }

    @Override
    public int size() {
        return 48;
    }

    public long getPreHeight() {
        return preHeight;
    }

    public void setPreHeight(long preHeight) {
        this.preHeight = preHeight;
    }
}
