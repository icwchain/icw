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
import io.icw.block.message.HashMessage;
import io.icw.block.rpc.call.NetworkCall;
import io.icw.block.service.BlockService;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.logback.NulsLogger;

import static io.icw.block.constant.CommandConstant.BLOCK_MESSAGE;
import static io.icw.block.constant.CommandConstant.GET_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage},用于孤儿链的维护
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("GetBlockHandlerV1")
public class GetBlockHandler implements MessageProcessor {

    @Autowired
    private BlockService service;

    private void sendBlock(int chainId, Block block, String nodeId, NulsHash requestHash) {
        BlockMessage message = new BlockMessage();
        message.setRequestHash(requestHash);
        if (block != null) {
            message.setBlock(block);
        }
        message.setSyn(false);
        NetworkCall.sendToNode(chainId, message, nodeId, BLOCK_MESSAGE);
    }

    @Override
    public String getCmd() {
        return GET_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HashMessage message = RPCUtil.getInstanceRpcStr(msgStr, HashMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        NulsHash requestHash = message.getRequestHash();
        logger.debug("recieve " + message + " from node-" + nodeId + ", hash:" + requestHash);
        Block block = service.getBlock(chainId, requestHash);
        if (block == null) {
            logger.debug("recieve invalid " + message + " from node-" + nodeId + ", hash:" + requestHash);
        }
        sendBlock(chainId, block, nodeId, requestHash);
    }
}