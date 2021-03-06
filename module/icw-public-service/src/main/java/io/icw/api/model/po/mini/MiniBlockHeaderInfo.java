package io.icw.api.model.po.mini;

import io.icw.api.model.po.BlockHeaderInfo;

import java.math.BigInteger;

public class MiniBlockHeaderInfo {

    private long height;

    private long createTime;

    private int txCount;

    private String agentHash;

    private String agentId;

    private String agentAlias;

    private int size;

    private BigInteger reward;


    public MiniBlockHeaderInfo() {
    }

    public MiniBlockHeaderInfo(BlockHeaderInfo info) {
        this.height = info.getHeight();
        this.createTime = info.getCreateTime();
        this.txCount = info.getTxCount();
        this.agentHash = info.getAgentHash();
        this.agentId = info.getAgentId();
        this.agentAlias = info.getAgentAlias();
        this.size = info.getSize();
        this.reward = info.getReward();
    }


    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentAlias() {
        return agentAlias;
    }

    public void setAgentAlias(String agentAlias) {
        this.agentAlias = agentAlias;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public BigInteger getReward() {
        return reward;
    }

    public void setReward(BigInteger reward) {
        this.reward = reward;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }
}
