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
import io.icw.base.data.Block;
import io.icw.base.data.NulsHash;
import io.icw.base.protocol.MessageProcessor;
import io.icw.block.manager.ContextManager;
import io.icw.block.message.BlockMessage;
import io.icw.block.message.HeightRangeMessage;
import io.icw.block.model.ChainContext;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.service.BlockService;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;

import static io.icw.block.constant.CommandConstant.BLOCK_MESSAGE;
import static io.icw.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;

/**
 * 处理收到的{@link HeightRangeMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("GetBlocksHandlerV1")
public class GetBlocksHandler implements MessageProcessor {

    @Autowired
    private BlockService service;

    private void sendBlock(int chainId, Block block, String nodeId, NulsHash requestHash) {
        BlockMessage blockMessage = new BlockMessage(requestHash, block, true);
        NetworkCall.sendToNode(chainId, blockMessage, nodeId, BLOCK_MESSAGE);
    }

    @Override
    public String getCmd() {
        return GET_BLOCKS_BY_HEIGHT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HeightRangeMessage message = RPCUtil.getInstanceRpcStr(msgStr, HeightRangeMessage.class);
        if (message == null) {
            return;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        long startHeight = message.getStartHeight();
        long endHeight = message.getEndHeight();
        if (startHeight < 0L || startHeight > endHeight || endHeight - startHeight > context.getParameters().getDownloadNumber()) {
            logger.error("PARAMETER_ERROR");
            return;
        }
//        logger.debug("recieve HeightRequestMessage from node-" + nodeId + ", start:" + startHeight + ", end:" + endHeight);
        NulsHash requestHash;
        try {
            requestHash = NulsHash.calcHash(message.serialize());
            Block block;
            do {
                block = service.getBlock(chainId, startHeight++);
                if (block == null) {
                    NetworkCall.sendFail(chainId, requestHash, nodeId);
                    return;
                }
                sendBlock(chainId, block, nodeId, requestHash);
            } while (endHeight >= startHeight);
            NetworkCall.sendSuccess(chainId, requestHash, nodeId);
        } catch (Exception e) {
            logger.error("error occur when send block", e);
        }
    }
}
