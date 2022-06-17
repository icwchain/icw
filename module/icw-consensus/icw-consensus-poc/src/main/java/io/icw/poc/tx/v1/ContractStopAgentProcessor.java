package io.icw.poc.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.JSONUtils;
import io.icw.poc.model.bo.Chain;
import io.icw.poc.model.bo.tx.txdata.Agent;
import io.icw.poc.model.bo.tx.txdata.RedPunishData;
import io.icw.poc.model.bo.tx.txdata.StopAgent;
import io.icw.poc.model.dto.transaction.TransactionDto;
import io.icw.poc.rpc.call.CallMethodUtils;
import io.icw.poc.utils.manager.AgentManager;
import io.icw.poc.utils.manager.ChainManager;
import io.icw.poc.utils.validator.TxValidator;
import io.icw.poc.constant.ConsensusErrorCode;
import io.icw.poc.utils.LoggerUtil;

import java.io.IOException;
import java.util.*;
/**
 * 智能合约停止节点处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("ContractStopAgentProcessorV1")
public class ContractStopAgentProcessor implements TransactionProcessor {

    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.CONTRACT_STOP_AGENT;
    }

    @Override
    public int getPriority() {
        return 7;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<NulsHash> hashSet = new HashSet<>();
        List<Transaction> redPunishTxList = txMap.get(TxType.RED_PUNISH);
        if(redPunishTxList != null && redPunishTxList.size() >0){
            for (Transaction redPunishTx:redPunishTxList) {
                RedPunishData redPunishData = new RedPunishData();
                try {
                    redPunishData.parse(redPunishTx.getTxData(), 0);
                    String addressHex = HexUtil.encode(redPunishData.getAddress());
                    redPunishAddressSet.add(addressHex);
                }catch (NulsException e){
                    chain.getLogger().error(e);
                }
            }
        }
        for (Transaction contractStopAgentTx:txs) {
            try {
                if(!txValidator.validateTx(chain, contractStopAgentTx)){
                    invalidTxList.add(contractStopAgentTx);
                    chain.getLogger().info("Intelligent Contract Exit Node Trading Verification Failed");
                    continue;
                }
                StopAgent stopAgent = new StopAgent();
                stopAgent.parse(contractStopAgentTx.getTxData(), 0);
                if (!hashSet.add(stopAgent.getCreateTxHash())) {
                    invalidTxList.add(contractStopAgentTx);
                    chain.getLogger().info("Repeated transactions");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    continue;
                }
                Agent agent = new Agent();
                if (stopAgent.getAddress() == null) {
                    Transaction createAgentTx = CallMethodUtils.getTransaction(chain, stopAgent.getCreateTxHash().toHex());
                    if (createAgentTx == null) {
                        invalidTxList.add(contractStopAgentTx);
                        chain.getLogger().info("The creation node transaction corresponding to intelligent contract cancellation node transaction does not exist");
                        errorCode = ConsensusErrorCode.AGENT_NOT_EXIST.getCode();
                        continue;
                    }
                    agent.parse(createAgentTx.getTxData(), 0);
                    stopAgent.setAddress(agent.getAgentAddress());
                }
                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(HexUtil.encode(stopAgent.getAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                        invalidTxList.add(contractStopAgentTx);
                        chain.getLogger().info("Intelligent contract cancellation node transaction cancellation node does not exist");
                        errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    }
                }
            }catch (NulsException e){
                invalidTxList.add(contractStopAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            }catch (IOException io){
                invalidTxList.add(contractStopAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(io);
                errorCode = ConsensusErrorCode.SERIALIZE_ERROR.getCode();
            }
        }
        result.put("txList", invalidTxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx:txs) {
            try {
                if(agentManager.stopAgentCommit(tx,blockHeader,chain)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create node transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    agentManager.stopAgentRollBack(rollbackTx, chain, blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create node transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx:txs) {
            try {
                try {
                    chain.getLogger().info("contract stop agent transaction rollback, hash is {}, tx is {}", tx.getHash().toHex(), JSONUtils.obj2json(new TransactionDto(tx)));
                } catch (Exception e) {
                    chain.getLogger().warn(e.getMessage());
                }
                if(agentManager.stopAgentRollBack(tx,chain,blockHeader)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to stop agent transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    agentManager.stopAgentCommit(commitTx, blockHeader, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to stop agent transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }
}
