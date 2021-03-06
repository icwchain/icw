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

package io.icw.block.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.data.*;
import io.icw.base.data.*;
import io.icw.base.protocol.MessageProcessor;
import io.icw.block.constant.BlockForwardEnum;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.TxGroupMessage;
import io.icw.block.model.CachedSmallBlock;
import io.icw.block.service.BlockService;
import io.icw.block.thread.monitor.TxGroupRequestor;
import io.icw.block.utils.BlockUtil;
import io.icw.block.utils.SmallBlockCacher;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;

import java.util.List;
import java.util.Map;

import static io.icw.block.constant.BlockForwardEnum.ERROR;
import static io.icw.block.constant.CommandConstant.TXGROUP_MESSAGE;

/**
 * 处理收到的{@link TxGroupMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("TxGroupHandlerV1")
public class TxGroupHandler implements MessageProcessor {

    @Autowired
    private BlockService blockService;

    @Override
    public String getCmd() {
        return TXGROUP_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        TxGroupMessage message = RPCUtil.getInstanceRpcStr(msgStr, TxGroupMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        List<Transaction> transactions = message.getTransactions();
        if (null == transactions || transactions.isEmpty()) {
            logger.warn("recieved a null txGroup form " + nodeId);
            return;
        }
//        logger.debug("recieve TxGroupMessage from network node-" + nodeId + ", txcount:" + transactions.size());
        NulsHash blockHash = message.getBlockHash();
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return;
        }
        //2.已收到部分区块,还缺失交易信息,收到的应该就是缺失的交易信息
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock cachedSmallBlock = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            if (cachedSmallBlock == null) {
                return;
            }
            SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
            BlockHeader header = smallBlock.getHeader();
            Map<NulsHash, Transaction> txMap = cachedSmallBlock.getTxMap();
            for (Transaction tx : transactions) {
                txMap.put(tx.getHash(), tx);
            }

            Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
            block.setNodeId(nodeId);
            TxGroupRequestor.removeTask(chainId, blockHash);
//            logger.debug("record recv block, block create time-" + DateUtils.timeStamp2DateStr(block.getHeader().getTime() * 1000) + ", hash-" + block.getHeader().getHash());
            boolean b = blockService.saveBlock(chainId, block, 1, true, false, true);
            if (!b) {
                SmallBlockCacher.setStatus(chainId, blockHash, ERROR);
            }
            return;
        }
        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            logger.error("It is theoretically impossible to enter this branch");
        }
    }
}
