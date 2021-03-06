package io.icw.api.utils;

import io.icw.api.model.po.CoinContextInfo;
import io.icw.api.ApiContext;
import io.icw.api.cache.ApiCache;
import io.icw.api.db.AgentService;
import io.icw.api.manager.CacheManager;
import io.icw.base.data.CoinData;
import io.icw.base.data.CoinTo;
import io.icw.core.core.ioc.SpringLiteContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetTool {

    public static Map getNulsAssets() {
        ApiCache apiCache = CacheManager.getCache(ApiContext.defaultChainId);
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("trades", coinContextInfo.getTxCount());
        map.put("totalAssets", AssetTool.toDouble(coinContextInfo.getTotal()));
        map.put("circulation", AssetTool.toDouble(coinContextInfo.getCirculation()));
        map.put("deposit", AssetTool.toDouble(coinContextInfo.getConsensusTotal()));
        map.put("business", AssetTool.toDouble(coinContextInfo.getBusiness()));
        map.put("team", AssetTool.toDouble(coinContextInfo.getTeam()));
        map.put("community", AssetTool.toDouble(coinContextInfo.getCommunity()));
        map.put("unmapped", AssetTool.toDouble(coinContextInfo.getUnmapped()));
        map.put("dailyReward", AssetTool.toDouble(coinContextInfo.getDailyReward()));
        map.put("destroy", AssetTool.toDouble(coinContextInfo.getDestroy()));
        int consensusCount = apiCache.getCurrentRound().getMemberCount() - apiCache.getChainInfo().getSeeds().size();
        if (consensusCount < 0) {
            consensusCount = 0;
        }
        map.put("consensusNodes", consensusCount);
        long count = 0;
        if (apiCache.getBestHeader() != null) {
            AgentService agentService = SpringLiteContext.getBean(AgentService.class);
            if (agentService != null) {
                count = agentService.agentsCount(ApiContext.defaultChainId, apiCache.getBestHeader().getHeight());
            }
        }
        map.put("totalNodes", count);
        return map;
    }

    public static Map getNulsAssetInfo() {
        ApiCache apiCache = CacheManager.getCache(ApiContext.defaultChainId);
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("trades", coinContextInfo.getTxCount());
        map.put("totalAssets", AssetTool.toCoinString(coinContextInfo.getTotal()));
        map.put("circulation", AssetTool.toCoinString(coinContextInfo.getCirculation()));
        map.put("deposit", AssetTool.toCoinString(coinContextInfo.getConsensusTotal()));
        map.put("business", AssetTool.toCoinString(coinContextInfo.getBusiness()));
        map.put("team", AssetTool.toCoinString(coinContextInfo.getTeam()));
        map.put("community", AssetTool.toCoinString(coinContextInfo.getCommunity()));
        map.put("unmapped", AssetTool.toCoinString(coinContextInfo.getUnmapped()));
        map.put("dailyReward", AssetTool.toCoinString(coinContextInfo.getDailyReward()));
        map.put("destroy", AssetTool.toCoinString(coinContextInfo.getDestroy()));
        int consensusCount = apiCache.getCurrentRound().getMemberCount() - apiCache.getChainInfo().getSeeds().size();
        if (consensusCount < 0) {
            consensusCount = 0;
        }
        map.put("consensusNodes", consensusCount);
        long count = 0;
        if (apiCache.getBestHeader() != null) {
            AgentService agentService = SpringLiteContext.getBean(AgentService.class);
            if (agentService != null) {
                count = agentService.agentsCount(ApiContext.defaultChainId, apiCache.getBestHeader().getHeight());
            }
        }
        map.put("totalNodes", count);
        return map;
    }

    public static String getTotal() {
        ApiCache apiCache = CacheManager.getCache(ApiContext.defaultChainId);
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        return AssetTool.toCoinString(coinContextInfo.getTotal());
    }

    public static String getCirculation() {
        ApiCache apiCache = CacheManager.getCache(ApiContext.defaultChainId);
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        return AssetTool.toCoinString(coinContextInfo.getCirculation());
    }

    public static double toDouble(BigInteger value) {
        return new BigDecimal(value).movePointLeft(8).setScale(8, RoundingMode.HALF_DOWN).doubleValue();
    }

    public static String toCoinString(BigInteger value) {
        BigDecimal decimal = new BigDecimal(value).movePointLeft(8).setScale(8, RoundingMode.HALF_DOWN);
        DecimalFormat format = new DecimalFormat("0.########");
        return format.format(decimal);
    }

    public static String[][] extractMultyAssetInfoFromCallTransaction(CoinData coinData) {
        List<CoinTo> toList = coinData.getTo();
        if (toList == null || toList.isEmpty()) {
            return null;
        }
        List<String[]> list = null;
        for (CoinTo to : toList) {
            if (to.getAssetsChainId() == ApiContext.defaultChainId && to.getAssetsId() == ApiContext.defaultAssetId) {
                continue;
            }
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(new String[]{to.getAmount().toString(), String.valueOf(to.getAssetsChainId()), String.valueOf(to.getAssetsId())});
        }
        String[][] array = null;
        if (list != null && !list.isEmpty()) {
            array = new String[list.size()][];
            int i = 0;
            for (String[] asset : list) {
                array[i++] = asset;
            }
        }
        return array;
    }
}
