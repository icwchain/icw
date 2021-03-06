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

package io.icw.v2.model.dto;


import io.icw.core.rpc.model.ApiModel;
import io.icw.core.rpc.model.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @author: Niels Wang
 */
@ApiModel(description = "blockHeader 区块头信息, 只返回对应的部分数据")
public class BlockHeaderDto {

    @ApiModelProperty(description = "区块的hash值")
    private String hash;

    @ApiModelProperty(description = "上一个区块的hash值")
    private String preHash;

    @ApiModelProperty(description = "梅克尔hash")
    private String merkleHash;

    @ApiModelProperty(description = "区块生成时间")
    private String time;

    @ApiModelProperty(description = "区块高度")
    private long height;

    @ApiModelProperty(description = "区块打包交易数量")
    private int txCount;

    @ApiModelProperty(description = "签名Hex.encode(byte[])")
    private String blockSignature;

    @ApiModelProperty(description = "大小")
    private int size;

    @ApiModelProperty(description = "打包地址")
    private String packingAddress;

    @ApiModelProperty(description = "共识轮次")
    private long roundIndex;

    @ApiModelProperty(description = "参与共识成员数量")
    private int consensusMemberCount;

    @ApiModelProperty(description = "当前共识轮开始时间")
    private String roundStartTime;

    @ApiModelProperty(description = "当前轮次打包出块的名次")
    private int packingIndexOfRound;

    @ApiModelProperty(description = "主网当前生效的版本")
    private int mainVersion;

    @ApiModelProperty(description = "区块的版本，可以理解为本地钱包的版本")
    private int blockVersion;

    @ApiModelProperty(description = "智能合约世界状态根")
    private String stateRoot;

    @ApiModelProperty(description = "区块打包交易的hash集合")
    private List<String> txHashList;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreHash() {
        return preHash;
    }

    public void setPreHash(String preHash) {
        this.preHash = preHash;
    }

    public String getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(String merkleHash) {
        this.merkleHash = merkleHash;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public String getBlockSignature() {
        return blockSignature;
    }

    public void setBlockSignature(String blockSignature) {
        this.blockSignature = blockSignature;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public int getConsensusMemberCount() {
        return consensusMemberCount;
    }

    public void setConsensusMemberCount(int consensusMemberCount) {
        this.consensusMemberCount = consensusMemberCount;
    }

    public String getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(String roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public int getMainVersion() {
        return mainVersion;
    }

    public void setMainVersion(int mainVersion) {
        this.mainVersion = mainVersion;
    }

    public int getBlockVersion() {
        return blockVersion;
    }

    public void setBlockVersion(int blockVersion) {
        this.blockVersion = blockVersion;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public static BlockHeaderDto mapToPojo(Map map) {
        BlockHeaderDto dto = new BlockHeaderDto();
        dto.hash = (String) map.get("hash");
        dto.preHash = (String) map.get("preHash");
        dto.merkleHash = (String) map.get("merkleHash");
        dto.time = (String) map.get("time");
        dto.height = Long.parseLong(map.get("height").toString());
        dto.txCount = (int) map.get("txCount");
        dto.blockSignature = (String) map.get("blockSignature");
        dto.size = (int) map.get("size");
        dto.packingAddress = (String) map.get("packingAddress");
        dto.roundIndex = Long.parseLong(map.get("roundIndex").toString());
        dto.consensusMemberCount = (int) map.get("consensusMemberCount");
        dto.roundStartTime = (String) map.get("roundStartTime");
        dto.packingIndexOfRound = (int) map.get("packingIndexOfRound");
        dto.mainVersion = (int) map.get("mainVersion");
        dto.blockVersion = (int) map.get("blockVersion");
        dto.stateRoot = (String) map.get("stateRoot");
        dto.setTxHashList((List<String>) map.get("txHashList"));
        return dto;
    }

    public List<String> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<String> txHashList) {
        this.txHashList = txHashList;
    }
}