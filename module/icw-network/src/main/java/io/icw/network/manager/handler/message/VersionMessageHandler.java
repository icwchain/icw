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

package io.icw.network.manager.handler.message;

import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.network.constant.NodeConnectStatusEnum;
import io.icw.network.constant.NodeStatusEnum;
import io.icw.network.manager.MessageFactory;
import io.icw.network.manager.MessageManager;
import io.icw.network.manager.NodeGroupManager;
import io.icw.network.manager.TimeManager;
import io.icw.network.manager.handler.base.BaseMessageHandler;
import io.icw.network.model.NetworkEventResult;
import io.icw.network.model.Node;
import io.icw.network.model.NodeGroup;
import io.icw.network.model.dto.BestBlockInfo;
import io.icw.network.model.message.VerackMessage;
import io.icw.network.model.message.VersionMessage;
import io.icw.network.model.message.base.BaseMessage;
import io.icw.network.model.message.body.VerackMessageBody;
import io.icw.network.model.message.body.VersionMessageBody;
import io.icw.network.netty.container.NodesContainer;
import io.icw.network.rpc.call.BlockRpcService;
import io.icw.network.rpc.call.impl.BlockRpcServiceImpl;
import io.icw.network.utils.LoggerUtil;

import java.util.Map;

/**
 * version message handler
 * client ?????????version?????????server???????????????
 * 1 ??????????????? 2.???????????????????????????????????????
 *
 * @author lan
 * @date 2018/10/20
 */
public class VersionMessageHandler extends BaseMessageHandler {

    private static VersionMessageHandler instance = new VersionMessageHandler();
    private NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return instance;
    }

    /**
     * ???????????????????????????
     * 1. ??????????????????????????????
     * 2. ????????????????????????????????????????????????????????????????????????
     * 3. ?????????IP????????????????????????n???
     *
     * @param ip
     * @param port
     * @return boolean
     */
    private boolean canConnectIn(int chainId, NodesContainer nodesContainer, int maxInCount, int sameIpMaxCount, String ip, int port) {

        int size = nodesContainer.getConnectedCount(Node.IN);
        if (size >= maxInCount) {
            LoggerUtil.logger(chainId).info("refuse canConnectIn size={},maxInCount={},node={}:{}", size, maxInCount, ip, port);
            return false;
        }

        Map<String, Node> connectedNodes = nodesContainer.getConnectedNodes();

        int sameIpCount = 0;
        for (Node node : connectedNodes.values()) {
            //????????????????????????????????????????????????????????????????????????????????????
            //if(ip.equals(node.getIp()) && (node.getPort().intValue() == port || node.getType() == Node.OUT)) {
            if (ip.equals(node.getIp()) && node.getType() == Node.OUT) {
                //???????????????????????????????????????????????????
                //????????????????????????????????????????????????????????????????
                LoggerUtil.logger(chainId).info("refuse canConnectIn ip={},node.getIp()={}, node.getType={}", ip, node.getIp(), node.getType());
                return false;
            }
            if (ip.equals(node.getIp())) {
                sameIpCount++;
            }
            if (sameIpCount >= sameIpMaxCount) {
                LoggerUtil.logger(chainId).info("refuse canConnectIn ip={},sameIpCount={},sameIpMaxCount={}, node.getType={}", ip, sameIpCount, sameIpMaxCount, node.getType());
                return false;
            }
        }

        return true;
    }

    /**
     * server recieve handler
     *
     * @param message message
     * @param node    Node
     */
    private void serverRecieveHandler(BaseMessage message, Node node) {
        VersionMessageBody versionBody = (VersionMessageBody) message.getMsgBody();
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(message.getHeader().getMagicNumber());
        String myIp = versionBody.getAddrYou().getIp().getHostAddress();
        int myPort = versionBody.getAddrYou().getPort();
        //??????magicNumber
        node.setMagicNumber(nodeGroup.getMagicNumber());
        String ip = node.getIp();
        node.setExternalIp(myIp);
        int maxIn;
        NodesContainer nodesContainer = null;
        int sameIpMaxCount = nodeGroup.getSameIpMaxCount(node.isCrossConnect());
        if (node.isCrossConnect()) {
            //???????????????magic????????????????????????????????????,??????magicNumber?????????????????????
            if (nodeGroup.isMoonGroup()) {
                LoggerUtil.logger(nodeGroup.getChainId()).error("node={} version canConnectIn fail..Cross=true, but group is moon net", node.getId());
                node.getChannel().close();
                return;
            } else {
                //?????????????????????????????????????????????????????????
                BlockRpcService blockRpcService = SpringLiteContext.getBean(BlockRpcServiceImpl.class);
                if (nodeGroup.isMoonNode()) {
                    //???????????????????????????????????????????????????????????????0?????????
                } else {
                    //????????????????????????????????????0
                    if (!nodeGroup.isHadBlockHeigh()) {
                        BestBlockInfo bestBlockInfo = blockRpcService.getBestBlockHeader(nodeGroup.getChainId());
                        if (bestBlockInfo.getBlockHeight() < 1) {
                            LoggerUtil.logger(nodeGroup.getChainId()).error("node={} version canConnectIn fail..Cross=true, but blockHeight={}", bestBlockInfo.getBlockHeight());
                            node.getChannel().close();
                            return;
                        } else {
                            nodeGroup.setHadBlockHeigh(true);
                        }
                    }
                }
            }
            maxIn = nodeGroup.getMaxCrossIn();
            nodesContainer = nodeGroup.getCrossNodeContainer();
        } else {
            maxIn = nodeGroup.getMaxIn();
            nodesContainer = nodeGroup.getLocalNetNodeContainer();
        }

        if (!canConnectIn(nodeGroup.getChainId(), nodesContainer, maxIn, sameIpMaxCount, node.getIp(), node.getRemotePort())) {
            LoggerUtil.logger(nodeGroup.getChainId()).info("node={} version canConnectIn fail...cross={}", node.getId(), node.isCrossConnect());
            node.getChannel().close();
            return;
        }
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.markCanuseNodeByIp(ip, NodeStatusEnum.AVAILABLE);
        //???????????????????????????
        node.setDisconnectListener(() -> {
            if (node.isCrossConnect()) {
                nodeGroup.getCrossNodeContainer().getConnectedNodes().remove(node.getId());
                nodeGroup.getCrossNodeContainer().markCanuseNodeByIp(ip, NodeStatusEnum.CONNECTABLE);
            } else {
                nodeGroup.getLocalNetNodeContainer().getConnectedNodes().remove(node.getId());
                nodeGroup.getLocalNetNodeContainer().markCanuseNodeByIp(ip, NodeStatusEnum.CONNECTABLE);
            }

        });
        //?????????????????????,?????????????????????????????????????????????
        node.setVersionProtocolInfos(versionBody.getProtocolVersion(), versionBody.getBlockHeight(), versionBody.getBlockHash());
        //??????version
        VersionMessage versionMessage = MessageFactory.getInstance().buildVersionMessage(node, message.getHeader().getMagicNumber());
        LoggerUtil.logger(nodeGroup.getChainId()).info("rec node={} ver msg success.go response versionMessage..cross={}", node.getId(), node.isCrossConnect());
        send(versionMessage, node, true);
    }

    /**
     * client recieve handler
     *
     * @param message message
     * @param node    Node
     */
    private void clientRecieveHandler(BaseMessage message, Node node) {
        VersionMessageBody versionBody = (VersionMessageBody) message.getMsgBody();
        String myIp = versionBody.getAddrYou().getIp().getHostAddress();
        int myPort = versionBody.getAddrYou().getPort();
        //??????magicNumber
        node.setExternalIp(myIp);
        //client??????version??????????????????server?????????????????????
//       Log.debug("VersionMessageHandler Recieve:Client"+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        //?????????????????????
        node.setVersionProtocolInfos(versionBody.getProtocolVersion(), versionBody.getBlockHeight(), versionBody.getBlockHash());
        node.setConnectStatus(NodeConnectStatusEnum.AVAILABLE);
        node.setFailCount(0);
        node.setConnectTime(TimeManager.currentTimeMillis());
        if (node.isCrossConnect()) {
            node.getNodeGroup().getCrossNodeContainer().setLatestHandshakeSuccTime(TimeManager.currentTimeMillis());
        } else {
            node.getNodeGroup().getLocalNetNodeContainer().setLatestHandshakeSuccTime(TimeManager.currentTimeMillis());
        }
        //client:?????????server??????????????????verack??????
        VerackMessage verackMessage = MessageFactory.getInstance().buildVerackMessage(node, message.getHeader().getMagicNumber(), VerackMessageBody.VER_SUCCESS);
        LoggerUtil.logger(node.getNodeGroup().getChainId()).info("rec node={} ver msg success.go response verackMessage..cross={}", node.getId(), node.isCrossConnect());
        MessageManager.getInstance().sendHandlerMsg(verackMessage, node, true);
        if (node.isSeedNode()) {
            //???????????????????????????
            MessageManager.getInstance().sendGetAddressMessage(node, false, false, true);
        }
    }

    /**
     * ??????????????????
     * Receive message processing
     *
     * @param message message
     * @param node    Node
     * @return NetworkEventResult
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
        int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(message.getHeader().getMagicNumber());
        LoggerUtil.logger(chainId).info("VersionMessageHandler recieve:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        if (Node.IN == node.getType()) {
            serverRecieveHandler(message, node);
        } else {
            clientRecieveHandler(message, node);
        }
        return NetworkEventResult.getResultSuccess();
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean asyn) {
        int chainId = NodeGroupManager.getInstance().getChainIdByMagicNum(message.getHeader().getMagicNumber());
        LoggerUtil.logger(chainId).info("VersionMessageHandler send:" + (node.isServer() ? "Server" : "Client") + ":" + node.getIp() + ":" + node.getRemotePort() + "==CMD=" + message.getHeader().getCommandStr());
        VersionMessage versionMessage = (VersionMessage) message;
        if (node.isCrossConnect()) {
            //??????????????????????????????cross chain no request block info
            versionMessage.getMsgBody().setBlockHash("");
            versionMessage.getMsgBody().setBlockHeight(0);
        } else {
            BlockRpcService blockRpcService = SpringLiteContext.getBean(BlockRpcServiceImpl.class);
            BestBlockInfo bestBlockInfo = blockRpcService.getBestBlockHeader(chainId);
            versionMessage.getMsgBody().setBlockHash(bestBlockInfo.getHash());
            versionMessage.getMsgBody().setBlockHeight(bestBlockInfo.getBlockHeight());
        }
        return super.send(message, node, asyn);
    }
}
