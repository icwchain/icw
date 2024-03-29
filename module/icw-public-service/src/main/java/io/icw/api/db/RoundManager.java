package io.icw.api.db;

import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.cache.ApiCache;
import io.icw.api.db.mongo.MongoAgentServiceImpl;
import io.icw.api.db.mongo.MongoBlockServiceImpl;
import io.icw.api.db.mongo.MongoDepositServiceImpl;
import io.icw.api.db.mongo.MongoRoundServiceImpl;
import io.icw.api.manager.CacheManager;
import io.icw.api.utils.AgentSorter;
import io.icw.api.ApiContext;
import io.icw.api.model.po.*;
import io.icw.base.basic.AddressTool;
import io.icw.core.basic.Result;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.Sha256Hash;
import io.icw.core.log.Log;
import io.icw.core.model.ArraysTool;
import io.icw.core.model.DoubleUtils;
import io.icw.core.parse.SerializeUtils;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

import java.math.BigInteger;
import java.util.*;

@Component
public class RoundManager {

    @Autowired
    private MongoAgentServiceImpl mongoAgentServiceImpl;

    @Autowired
    private MongoDepositServiceImpl mongoDepositServiceImpl;

    @Autowired
    private MongoRoundServiceImpl mongoRoundServiceImpl;

    @Autowired
    private MongoBlockServiceImpl mongoBlockServiceImpl;

    public void process(int chainId, BlockInfo blockInfo) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();

        if (null == currentRound.getItemList()) {
            PocRound round = null;
            long roundIndex = blockInfo.getHeader().getRoundIndex();
            while (round == null && blockInfo.getHeader().getHeight() > 1) {
                round = mongoRoundServiceImpl.getRound(chainId, roundIndex--);
            }
            if (round != null) {
                CurrentRound preRound = new CurrentRound();
                preRound.initByPocRound(round);
                List<PocRoundItem> list = mongoRoundServiceImpl.getRoundItemList(chainId, round.getIndex());
                preRound.setItemList(list);
                preRound.setStartBlockHeader(mongoBlockServiceImpl.getBlockHeader(chainId, round.getStartHeight()));
                preRound.setPackerOrder(round.getMemberCount());
                apiCache.setCurrentRound(preRound);
            }
        }
        if (blockInfo.getHeader().getRoundIndex() == apiCache.getCurrentRound().getIndex()) {
            processCurrentRound(chainId, blockInfo);
        } else {
            processNextRound(chainId, blockInfo);
        }
    }

    private void processCurrentRound(int chainId, BlockInfo blockInfo) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();
        int indexOfRound = blockInfo.getHeader().getPackingIndexOfRound();
        //下一个出块者
        currentRound.setPackerOrder(indexOfRound < currentRound.getMemberCount() ? indexOfRound + 1 : indexOfRound);
        BlockHeaderInfo header = blockInfo.getHeader();
        
        try {
	        PocRoundItem item = currentRound.getItemList().get(indexOfRound - 1);
	        item.setTime(header.getCreateTime());
	        item.setBlockHeight(header.getHeight());
	        item.setBlockHash(header.getHash());
	        item.setTxCount(header.getTxCount());
	        item.setReward(header.getReward());
	        mongoRoundServiceImpl.updateRoundItem(chainId, item);
        } catch (Exception e) {
			Log.error(e);
		}
        
        currentRound.setProducedBlockCount(currentRound.getProducedBlockCount() + 1);
        currentRound.setEndHeight(blockInfo.getHeader().getHeight());
        currentRound.setLostRate(DoubleUtils.div(header.getPackingIndexOfRound() - currentRound.getProducedBlockCount(), header.getPackingIndexOfRound()));
        this.fillPunishCount(blockInfo.getTxList(), currentRound, true);

        apiCache.setCurrentRound(currentRound);
        
        this.mongoRoundServiceImpl.updateRound(chainId, currentRound.toPocRound());
        ApiContext.addAndRemoveLastRound(currentRound);
    }


    private void fillPunishCount(List<TransactionInfo> txs, CurrentRound round, boolean add) {
        int redCount = 0;
        int yellowCount = 0;
        for (TransactionInfo tx : txs) {
            if (tx.getType() == TxType.YELLOW_PUNISH) {
                yellowCount += tx.getTxDataList() != null ? tx.getTxDataList().size() : 0;
            } else if (tx.getType() == TxType.RED_PUNISH) {
                redCount++;
            }
        }
        if (add) {
            round.setYellowCardCount(round.getYellowCardCount() + yellowCount);
            round.setRedCardCount(round.getRedCardCount() + redCount);
        } else {
            round.setYellowCardCount(round.getYellowCardCount() - yellowCount);
            round.setRedCardCount(round.getRedCardCount() - redCount);
        }
    }

    private void processNextRound(int chainId, BlockInfo blockInfo) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();
        BlockHeaderInfo header = blockInfo.getHeader();
        long startHeight = currentRound.getStartHeight();
        if (null != currentRound.getStartBlockHeader() && currentRound.getStartBlockHeader().getPackingIndexOfRound() > 1) {
            startHeight = startHeight - 1;
        }
        
        List<String> members = new ArrayList<String>();
		try {
			Map<String, Object> callParams = new HashMap<>(4);
	        callParams.put("roundIndex", header.getRoundIndex());
	        callParams.put("blockHeight", header.getHeight());
	        callParams.put("isRealTime", true); 
			Response cmdResp = ResponseMessageProcessor.requestAndResponse("pow", "pow_roundMembers", callParams);
			Log.info(cmdResp.getResponseErrorCode() 
					+ " : " 
					+ cmdResp.getResponseStatus()
					+ " : " 
					+ cmdResp.getResponseComment()
					+ " : " 
					+ cmdResp.getResponseData());
			
			members = (List<String>)((HashMap) cmdResp.getResponseData()).get("pow_roundMembers");
			Log.info(members.toString());
		} catch (Exception e) {
			Log.error(e);
		}
		
        List<AgentInfo> agentList = mongoAgentServiceImpl.getAgentList(chainId, startHeight);
        List<DepositInfo> depositList = mongoDepositServiceImpl.getDepositList(chainId, startHeight);
        Map<String, AgentInfo> map = new HashMap<>();
        Map<String, BigInteger> depositMap = new HashMap<>();
        for (AgentInfo agent : agentList) {
            map.put(agent.getTxHash(), agent);
        }
        for (DepositInfo deposit : depositList) {
            BigInteger agentDeposit = depositMap.get(deposit.getAgentHash());
            if (null == agentDeposit) {
                agentDeposit = BigInteger.ZERO;
            }
            depositMap.put(deposit.getAgentHash(), agentDeposit.add(deposit.getAmount()));
        }
        List<AgentSorter> sorterList = new ArrayList<>();
        for (AgentInfo agent : map.values()) {
        	if (!members.isEmpty() && !members.contains(agent.getPackingAddress())) {
        		Log.info("pass: " + agent.getPackingAddress());
        		continue;
        	}
            BigInteger totalDeposit = depositMap.get(agent.getTxHash());
            if (null == totalDeposit) {
                totalDeposit = BigInteger.ZERO;
            }
            if (totalDeposit.compareTo(ApiContext.minDeposit) >= 0) {
                AgentSorter sorter = new AgentSorter();
                sorter.setAgentId(agent.getTxHash());
                byte[] hash = ArraysTool.concatenate(AddressTool.getAddress(agent.getPackingAddress()), SerializeUtils.uint64ToByteArray(blockInfo.getHeader().getRoundStartTime()));

                sorter.setSorter(Sha256Hash.twiceOf(hash).toString());

                sorterList.add(sorter);
            }
        }

        for (String address : apiCache.getChainInfo().getSeeds()) {
        	if (!members.isEmpty() && !members.contains(address)) {
        		Log.info("pass: " + address);
        		continue;
        	}
            AgentSorter sorter = new AgentSorter();
            sorter.setSeedAddress(address);
            byte[] hash = ArraysTool.concatenate(AddressTool.getAddress(address), SerializeUtils.uint64ToByteArray(blockInfo.getHeader().getRoundStartTime()));
            sorter.setSorter(Sha256Hash.twiceOf(hash).toString());
            sorterList.add(sorter);
        }
        Collections.sort(sorterList);

        Result<Map> result = WalletRpcHandler.getConsensusConfig(chainId);
        Log.info(result.toString());
        Map configMap = result.getData();
        int packingInterval = Integer.valueOf(configMap.get("packingInterval").toString());
        
        //生成新的round
        CurrentRound round = new CurrentRound();
        round.setIndex(header.getRoundIndex());
        round.setStartHeight(header.getHeight());
        round.setStartBlockHeader(header);
        round.setStartTime(header.getRoundStartTime());
        round.setMemberCount(sorterList.size());
        round.setEndTime(round.getStartTime() + packingInterval * sorterList.size());
        round.setProducedBlockCount(1);

        List<PocRoundItem> itemList = new ArrayList<>();
        int index = 1;
        for (AgentSorter sorter : sorterList) {
            PocRoundItem item = new PocRoundItem();
            item.setRoundIndex(header.getRoundIndex());
            item.setOrder(index++);
            if (item.getOrder() == header.getPackingIndexOfRound()) {
                item.setTime(header.getCreateTime());
                item.setBlockHeight(header.getHeight());
                item.setBlockHash(header.getHash());
                item.setTxCount(header.getTxCount());
                item.setReward(header.getReward());
            }
            item.setId(item.getRoundIndex() + "_" + item.getOrder());
            if (null == sorter.getSeedAddress()) {
                AgentInfo agentInfo = map.get(sorter.getAgentId());
                item.setAgentName(agentInfo.getAgentAlias() == null ?
                        agentInfo.getTxHash().substring(agentInfo.getTxHash().length() - 8) : agentInfo.getAgentAlias());
                item.setAgentHash(agentInfo.getTxHash());
                item.setPackingAddress(agentInfo.getPackingAddress());
            } else {
                item.setSeedAddress(sorter.getSeedAddress());
                item.setPackingAddress(sorter.getSeedAddress());

            }
            item.setTime(round.getStartTime() + item.getOrder() * packingInterval);
            itemList.add(item);
        }
        round.setItemList(itemList);
        round.setMemberCount(itemList.size());
        round.setPackerOrder(header.getPackingIndexOfRound() + 1);

        round.setRedCardCount(0);
        round.setYellowCardCount(0);
        round.setLostRate(DoubleUtils.div(header.getPackingIndexOfRound() - round.getProducedBlockCount(), header.getPackingIndexOfRound()));

        fillPunishCount(blockInfo.getTxList(), round, true);
        if (round.getIndex() == 1) {
            CurrentRound round1 = new CurrentRound();
            round1.setStartTime(header.getRoundStartTime());
            round1.setStartHeight(0);
            round1.setProducedBlockCount(1);
            round1.setMemberCount(1);
            round1.setIndex(1);
            round1.setEndTime(header.getCreateTime());
            round1.setEndHeight(0);
            List<PocRoundItem> itemList1 = new ArrayList<>();
            PocRoundItem item = new PocRoundItem();
            itemList1.add(item);
            item.setTime(header.getCreateTime());
            item.setTxCount(1);
            item.setBlockHash(header.getHash());
            item.setBlockHeight(0);
            item.setPackingAddress(header.getPackingAddress());
            item.setRoundIndex(1);
            item.setOrder(1);
            round1.setItemList(itemList);
            round = round1;
        }
        apiCache.setCurrentRound(round);
//        Log.warn("++++++++{}({})+++++++" + round.toString(), blockInfo.getBlockHeader().getHeight(), startHeight);
        mongoRoundServiceImpl.saveRoundItemList(chainId, round.getItemList());
        PocRound pocRound = round.toPocRound();
        mongoRoundServiceImpl.saveRound(chainId, pocRound);
        ApiContext.addAndRemoveLastRound(currentRound);
    }


    public void rollback(int chainId, BlockInfo blockInfo) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();
        if (null == currentRound.getItemList()) {
            PocRound round = null;
            long roundIndex = blockInfo.getHeader().getRoundIndex();
            while (round == null && blockInfo.getHeader().getHeight() > 0) {
                if (roundIndex < 0) {
                    return;
                }
                round = mongoRoundServiceImpl.getRound(chainId, roundIndex--);
            }
            if (round != null) {
                CurrentRound preRound = new CurrentRound();
                preRound.initByPocRound(round);
                List<PocRoundItem> list = mongoRoundServiceImpl.getRoundItemList(chainId, round.getIndex());
                preRound.setItemList(list);
                preRound.setStartBlockHeader(mongoBlockServiceImpl.getBlockHeader(chainId, round.getStartHeight()));
                preRound.setPackerOrder(round.getMemberCount());
                currentRound = preRound;
                apiCache.setCurrentRound(currentRound);
            }
        }
        if (blockInfo.getHeader().getHeight() == currentRound.getStartHeight()) {
            rollbackPreRound(chainId);
        } else {
            rollbackCurrentRound(chainId, blockInfo);
        }
    }

    private void rollbackPreRound(int chainId) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();

        mongoRoundServiceImpl.removeRound(chainId, currentRound.getIndex());
        PocRound round = null;
        long roundIndex = currentRound.getIndex() - 1;
        if (currentRound.getStartHeight() == 1) {
            roundIndex = 1;
        }

        while (round == null) {
            if (roundIndex < 0) {
                return;
            }
            round = mongoRoundServiceImpl.getRound(chainId, roundIndex--);
        }
        CurrentRound preRound = new CurrentRound();
        preRound.initByPocRound(round);
        List<PocRoundItem> list = mongoRoundServiceImpl.getRoundItemList(chainId, round.getIndex());
        preRound.setItemList(list);
        preRound.setStartBlockHeader(mongoBlockServiceImpl.getBlockHeader(chainId, round.getStartHeight()));
        preRound.setPackerOrder(round.getMemberCount());
        apiCache.setCurrentRound(preRound);
    }

    private void rollbackCurrentRound(int chainId, BlockInfo blockInfo) {
        ApiCache apiCache = CacheManager.getCache(chainId);
        CurrentRound currentRound = apiCache.getCurrentRound();

        int indexOfRound = blockInfo.getHeader().getPackingIndexOfRound() - 1;
        if (currentRound.getItemList() == null) {
            PocRound round = mongoRoundServiceImpl.getRound(chainId, blockInfo.getHeader().getRoundIndex());
            CurrentRound preRound = new CurrentRound();
            preRound.initByPocRound(round);
            List<PocRoundItem> list = mongoRoundServiceImpl.getRoundItemList(chainId, round.getIndex());
            preRound.setItemList(list);
            preRound.setStartBlockHeader(mongoBlockServiceImpl.getBlockHeader(chainId, round.getStartHeight()));
            preRound.setPackerOrder(round.getMemberCount());
            apiCache.setCurrentRound(preRound);
        }
        PocRoundItem item = currentRound.getItemList().get(indexOfRound);
        item.setBlockHeight(0);
        item.setReward(BigInteger.ZERO);
        item.setTxCount(0);

        mongoRoundServiceImpl.updateRoundItem(chainId, item);
        currentRound.setProducedBlockCount(currentRound.getProducedBlockCount() - 1);
        currentRound.setEndHeight(blockInfo.getHeader().getHeight() - 1);
        currentRound.setLostRate(DoubleUtils.div(currentRound.getMemberCount() - currentRound.getProducedBlockCount(), currentRound.getMemberCount()));
        this.fillPunishCount(blockInfo.getTxList(), currentRound, false);

        this.mongoRoundServiceImpl.updateRound(chainId, currentRound.toPocRound());
        apiCache.setCurrentRound(currentRound);
    }
}
