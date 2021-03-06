package io.icw.transaction.message.handler;

import io.icw.base.RPCUtil;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.MessageProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.transaction.manager.ChainManager;
import io.icw.transaction.message.BroadcastTxMessage;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.po.TransactionNetPO;
import io.icw.transaction.service.TxService;
import io.icw.transaction.utils.TxDuplicateRemoval;

import static io.icw.transaction.constant.TxCmd.NW_RECEIVE_TX;
import static io.icw.transaction.utils.LoggerUtil.LOG;

/**
 * 接收处理网络中其他节点广播的完整交易的消息
 */
@Component("BroadcastTxMessageHandlerV1")
public class BroadcastTxMessageHandler implements MessageProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxService txService;

    @Override
    public String getCmd() {
        return NW_RECEIVE_TX;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = null;
        try {
            chain = chainManager.getChain(chainId);
            //根据区块同步状态,决定是否开始处理交易
            if(!chain.getProcessTxStatus().get()){
                return;
            }
            //解析新的交易消息
            BroadcastTxMessage message = RPCUtil.getInstanceRpcStr(msgStr, BroadcastTxMessage.class);
            if (message == null) {
                return;
            }
            Transaction transaction = message.getTx();
            String hash = transaction.getHash().toHex();
            //交易缓存中是否已存在该交易hash
            boolean rs = TxDuplicateRemoval.insertAndCheck(hash);
            //记录向本节点发送完整交易的其他网络节点，转发hash时排除掉
            TxDuplicateRemoval.putExcludeNode(hash, nodeId);
            if (!rs) {
                //该完整交易已经收到过
                return;
            }
            //将交易放入待验证本地交易队列中
            txService.newBroadcastTx(chainManager.getChain(chainId), new TransactionNetPO(transaction, nodeId));
        } catch (Exception e) {
            errorLogProcess(chain, e);
        }
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
