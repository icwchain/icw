/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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
package io.icw.api.model.entity;


import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.Address;
import io.icw.base.data.BaseNulsData;
import io.icw.base.data.NulsHash;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * 节点信息类
 * Node information class
 *
 * @author tag
 * 2018/11/6
 */
public class Agent extends BaseNulsData {

    /**
     * 节点地址
     * agent address
     **/
    private byte[] agentAddress;

    /**
     * 打包地址
     * packing address
     **/
    private byte[] packingAddress;

    /**
     * 奖励地址
     * reward address
     */
    private byte[] rewardAddress;

    /**
     * 保证金
     * deposit
     */
    private BigInteger deposit;

    /**
     * 佣金比例
     * commission rate
     */
    private byte commissionRate;

    /**
     * 创建时间
     * create time
     **/
    private transient long time;

    /**
     * 所在区块高度
     * block height
     */
    private transient long blockHeight = -1L;

    /**
     * 该节点注销所在区块高度
     * Block height where the node logs out
     */
    private transient long delHeight = -1L;

    /**
     * 0:待共识 unConsensus, 1:共识中 consensus
     */
    private transient int status;

    /**
     * 信誉值
     * credit value
     */
    private transient double creditVal;

    /**
     * 总委托金额
     * Total amount entrusted
     */
    private transient BigInteger totalDeposit = BigInteger.ZERO;

    /**
     * 交易HASH
     * transaction hash
     */
    private transient NulsHash txHash;

    /**
     * 参与共识人数
     * Participation in consensus
     */
    private transient int memberCount;

    /**
     * 别名不序列化
     * Aliases not serialized
     */
    private transient String alais;

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBigInteger();
        size += this.agentAddress.length;
        size += this.rewardAddress.length;
        size += this.packingAddress.length;
        size += 1;
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBigInteger(deposit);
        stream.write(agentAddress);
        stream.write(packingAddress);
        stream.write(rewardAddress);
        stream.write(this.commissionRate);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.deposit = byteBuffer.readBigInteger();
        this.agentAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.packingAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.rewardAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.commissionRate = byteBuffer.readByte();
    }


    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(byte commissionRate) {
        this.commissionRate = commissionRate;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public double getCreditVal() {
        return creditVal < 0d ? 0D : this.creditVal;
    }

    public double getRealCreditVal() {
        return this.creditVal;
    }

    public void setTxHash(NulsHash txHash) {
        this.txHash = txHash;
    }

    public NulsHash getTxHash() {
        return txHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public byte[] getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(byte[] agentAddress) {
        this.agentAddress = agentAddress;
    }

    public byte[] getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(byte[] rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public BigInteger getDeposit() {
        return deposit;
    }

    public void setDeposit(BigInteger deposit) {
        this.deposit = deposit;
    }

    public BigInteger getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigInteger totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    @Override
    public Agent clone() throws CloneNotSupportedException {
        return (Agent) super.clone();
    }

    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(this.agentAddress);
        return addressSet;
    }

    public String getAlais() {
        return alais;
    }

    public void setAlais(String alais) {
        this.alais = alais;
    }
}
