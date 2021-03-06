package io.icw.api.cache;

import io.icw.api.model.po.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApiCache {

    private ChainInfo chainInfo;

    private ChainConfigInfo configInfo;

    private CoinContextInfo coinContextInfo;

    private BlockHeaderInfo bestHeader;

    private CurrentRound currentRound;

    private Map<String, AccountInfo> accountMap = new ConcurrentHashMap<>();

    private Map<String, AccountLedgerInfo> ledgerMap = new ConcurrentHashMap<>();

    private Map<String, AgentInfo> agentMap = new ConcurrentHashMap<>();

    private Map<String, AliasInfo> aliasMap = new ConcurrentHashMap<>();

    private List<Nrc20Info> nrc20InfoList = new ArrayList<>();

    private List<Nrc721Info> nrc721InfoList = new ArrayList<>();

    public ApiCache() {
        currentRound = new CurrentRound();
    }

    public void addAccountInfo(AccountInfo accountInfo) {
        accountMap.put(accountInfo.getAddress(), accountInfo);
    }

    public AccountInfo getAccountInfo(String address) {
        return accountMap.get(address);
    }

    public AccountLedgerInfo getAccountLedgerInfo(String key) {
        return ledgerMap.get(key);
    }

    public void addAccountLedgerInfo(AccountLedgerInfo ledgerInfo) {
        ledgerMap.put(ledgerInfo.getKey(), ledgerInfo);
    }

    public void addNrc20Info(Nrc20Info nrc20Info) {
        nrc20InfoList.add(nrc20Info);
    }

    public List<Nrc20Info> getNrc20InfoList() {
        return nrc20InfoList;
    }

    public List<Nrc721Info> getNrc721InfoList() {
        return nrc721InfoList;
    }

    public void addNrc721Info(Nrc721Info nrc721Info) {
        nrc721InfoList.add(nrc721Info);
    }

    public void addAgentInfo(AgentInfo agentInfo) {
        agentMap.put(agentInfo.getTxHash(), agentInfo);
    }

    public AgentInfo getAgentInfo(String agentHash) {
        return agentMap.get(agentHash);
    }

    public void addAlias(AliasInfo aliasInfo) {
        aliasMap.put(aliasInfo.getAddress(), aliasInfo);
        aliasMap.put(aliasInfo.getAlias(), aliasInfo);
    }

    public AliasInfo getAlias(String key) {
        return aliasMap.get(key);
    }


    public ChainInfo getChainInfo() {
        return chainInfo;
    }

    public void setChainInfo(ChainInfo chainInfo) {
        this.chainInfo = chainInfo;
    }


    public BlockHeaderInfo getBestHeader() {
        return bestHeader;
    }

    public void setBestHeader(BlockHeaderInfo bestHeader) {
        this.bestHeader = bestHeader;
    }

    public CurrentRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(CurrentRound currentRound) {
        this.currentRound = currentRound;
    }

    public Map<String, AccountInfo> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(Map<String, AccountInfo> accountMap) {
        this.accountMap = accountMap;
    }

    public Map<String, AccountLedgerInfo> getLedgerMap() {
        return ledgerMap;
    }

    public void setLedgerMap(Map<String, AccountLedgerInfo> ledgerMap) {
        this.ledgerMap = ledgerMap;
    }

    public Map<String, AgentInfo> getAgentMap() {
        return agentMap;
    }

    public void setAgentMap(Map<String, AgentInfo> agentMap) {
        this.agentMap = agentMap;
    }

    public Map<String, AliasInfo> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, AliasInfo> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public CoinContextInfo getCoinContextInfo() {
        return coinContextInfo;
    }

    public void setCoinContextInfo(CoinContextInfo coinContextInfo) {
        this.coinContextInfo = coinContextInfo;
    }

    public ChainConfigInfo getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(ChainConfigInfo configInfo) {
        this.configInfo = configInfo;
    }

}
