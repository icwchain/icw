package io.icw.crosschain.nuls.model.bo.config;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.base.data.BaseNulsData;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 跨链模块配置类
 * Consensus Module Configuration Class
 *
 * @author tag
 * 2019/4/10
 */
public class ConfigBean extends BaseNulsData {
    /**
     * 资产ID
     * assets id
     */
    private int assetId;

    /**
     * chain id
     */
    private int chainId;

    /**
     * 最小链接数
     * Minimum number of links
     * */
    private int minNodeAmount;

    /**
     * 最大链接数
     * */
    private int maxOutAmount;

    /**
     * 最大被链接数
     * */
    private int maxInAmount;

    /**
     * 跨链交易被打包多少块之后广播给其他链
     * */
    private int sendHeight;

    /**
     * 拜占庭比例
     * */
    private int byzantineRatio;

    /**
     * 最小签名数
     * */
    private int minSignature;

    /**
     * 主网验证人信息
     * */
    private String verifiers;

    /**
     * 主网拜占庭比例
     * */
    private int mainByzantineRatio;

    /**
     * 主网最大签名验证数
     * */
    private int maxSignatureCount;

    /**
     * 主网验证人列表
     * */
    private Set<String> verifierSet = new HashSet<>();


    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMinNodeAmount() {
        return minNodeAmount;
    }

    public void setMinNodeAmount(int minNodeAmount) {
        this.minNodeAmount = minNodeAmount;
    }

    public int getSendHeight() {
        return sendHeight;
    }

    public void setSendHeight(int sendHeight) {
        this.sendHeight = sendHeight;
    }

    public int getByzantineRatio() {
        return byzantineRatio;
    }

    public void setByzantineRatio(int byzantineRatio) {
        this.byzantineRatio = byzantineRatio;
    }

    public int getMaxOutAmount() {
        return maxOutAmount;
    }

    public void setMaxOutAmount(int maxOutAmount) {
        this.maxOutAmount = maxOutAmount;
    }

    public int getMaxInAmount() {
        return maxInAmount;
    }

    public void setMaxInAmount(int maxInAmount) {
        this.maxInAmount = maxInAmount;
    }

    public int getMinSignature() {
        return minSignature;
    }

    public void setMinSignature(int minSignature) {
        this.minSignature = minSignature;
    }

    public String getVerifiers() {
        return verifiers;
    }

    public void setVerifiers(String verifiers) {
        this.verifiers = verifiers;
    }

    public int getMainByzantineRatio() {
        return mainByzantineRatio;
    }

    public void setMainByzantineRatio(int mainByzantineRatio) {
        this.mainByzantineRatio = mainByzantineRatio;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
    }

    public Set<String> getVerifierSet() {
        return verifierSet;
    }

    public void setVerifierSet(Set<String> verifierSet) {
        this.verifierSet = verifierSet;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(assetId);
        stream.writeUint16(chainId);
        stream.writeUint16(minNodeAmount);
        stream.writeUint16(maxOutAmount);
        stream.writeUint16(maxInAmount);
        stream.writeUint16(sendHeight);
        stream.writeUint16(byzantineRatio);
        stream.writeUint16(minSignature);
        stream.writeString(verifiers);
        stream.writeUint16(mainByzantineRatio);
        stream.writeUint16(maxSignatureCount);
        int registerCount = verifierSet == null ? 0 : verifierSet.size();
        stream.writeVarInt(registerCount);
        if(verifierSet != null){
            for (String registerAgent:verifierSet) {
                stream.writeString(registerAgent);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assetId = byteBuffer.readUint16();
        this.chainId = byteBuffer.readUint16();
        this.minNodeAmount = byteBuffer.readUint16();
        this.maxOutAmount = byteBuffer.readUint16();
        this.maxInAmount = byteBuffer.readUint16();
        this.sendHeight = byteBuffer.readUint16();
        this.byzantineRatio = byteBuffer.readUint16();
        this.minNodeAmount = byteBuffer.readUint16();
        this.verifiers = byteBuffer.readString();
        this.mainByzantineRatio = byteBuffer.readUint16();
        this.maxSignatureCount = byteBuffer.readUint16();
        int registerCount = (int) byteBuffer.readVarInt();
        if(registerCount > 0){
            Set<String> verifierSet = new HashSet<>();
            for (int i = 0; i < registerCount; i++) {
                verifierSet.add(byteBuffer.readString());
            }
            this.verifierSet = verifierSet;
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16() * 10;
        size += SerializeUtils.sizeOfString(verifiers);
        size += SerializeUtils.sizeOfVarInt(verifierSet == null ? 0 : verifierSet.size());
        if(verifierSet != null){
            for (String verifier:verifierSet) {
                size += SerializeUtils.sizeOfString(verifier);
            }
        }
        return size;
    }
}
