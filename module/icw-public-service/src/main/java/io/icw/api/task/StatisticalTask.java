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

package io.icw.api.task;

import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.db.mongo.MongoAgentServiceImpl;
import io.icw.api.db.mongo.MongoBlockServiceImpl;
import io.icw.api.db.mongo.MongoDepositServiceImpl;
import io.icw.api.db.mongo.MongoStatisticalServiceImpl;
import io.icw.api.model.po.AgentInfo;
import io.icw.api.model.po.BlockHeaderInfo;
import io.icw.api.model.po.DepositInfo;
import io.icw.api.model.po.StatisticalInfo;
import io.icw.api.utils.LoggerUtil;
import io.icw.api.db.AgentService;
import io.icw.api.db.BlockService;
import io.icw.api.db.DepositService;
import io.icw.api.db.StatisticalService;
import io.icw.core.basic.Result;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.model.DateUtils;
import io.icw.core.model.DoubleUtils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
public class StatisticalTask implements Runnable {

    private int chainId;

    private StatisticalService statisticalService;

    private BlockService blockService;

    private DepositService depositService;

    private AgentService agentService;

    public StatisticalTask(int chainId) {
        this.chainId = chainId;
        statisticalService = SpringLiteContext.getBean(MongoStatisticalServiceImpl.class);
        blockService = SpringLiteContext.getBean(MongoBlockServiceImpl.class);
        depositService = SpringLiteContext.getBean(MongoDepositServiceImpl.class);
        agentService = SpringLiteContext.getBean(MongoAgentServiceImpl.class);
    }

    @Override
    public void run() {
        try {
            this.doCalc();
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }

    private void doCalc() {
        long bestId = statisticalService.getBestId(chainId);
        BlockHeaderInfo header = blockService.getBestBlockHeader(chainId);
        if (null == header || header.getHeight() == 0) {
            return;
        }
        long day = 24 * 3600 * 1000;
        long start = bestId + 1;
        long end = 0;
        if (bestId == -1) {
            BlockHeaderInfo header0 = blockService.getBlockHeader(chainId, 1);
            start = header0.getCreateTime() * 1000 - 10 * DateUtils.SECOND_TIME;
            end = start + day;
            this.statisticalService.saveBestId(chainId, start);
        } else {
            end = start + day - 1;
        }
        while (true) {
            if (end > header.getCreateTime() * 1000) {
                break;
            }
            statistical(start, end);
            start = end + 1;
            end = end + day;
            BlockHeaderInfo newBlockHeader = blockService.getBestBlockHeader(chainId);
            if (null != newBlockHeader) {
                header = newBlockHeader;
            }
        }
    }

    private void statistical(long start, long end) {
        long txCount = statisticalService.calcTxCount(chainId, start / 1000, end / 1000);
        BigInteger consensusLocked = BigInteger.ZERO;
        long height = blockService.getMaxHeight(chainId, end / 1000);
        List<AgentInfo> agentList = agentService.getAgentList(chainId, height);
        List<DepositInfo> depositList = depositService.getDepositList(chainId, height);
        int nodeCount = agentList.size();
        for (AgentInfo agent : agentList) {
            consensusLocked = consensusLocked.add(agent.getDeposit());
        }
        for (DepositInfo deposit : depositList) {
            consensusLocked = consensusLocked.add(deposit.getAmount());
        }
        double annualizedReward = 0L;
        if (consensusLocked.compareTo(BigInteger.ZERO) != 0) {
            Result<Map> result = WalletRpcHandler.getConsensusConfig(chainId);
            Map map = result.getData();
            String inflationAmount = map.get("inflationAmount").toString();
            double d = DoubleUtils.mul(365, new BigInteger(inflationAmount).doubleValue());
            d = DoubleUtils.div(d, 30, 0);
            annualizedReward = DoubleUtils.mul(100, DoubleUtils.div(d, consensusLocked.doubleValue(), 4), 2);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(end);
        StatisticalInfo info = new StatisticalInfo();
        info.setTime(end);
        info.setTxCount(txCount);
        info.setAnnualizedReward(annualizedReward);
        info.setNodeCount(nodeCount);
        info.setConsensusLocked(consensusLocked);
        info.setDate(calendar.get(Calendar.DATE));
        info.setMonth(calendar.get(Calendar.MONTH) + 1);
        info.setYear(calendar.get(Calendar.YEAR));
        try {
            this.statisticalService.insert(chainId, info);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
        this.statisticalService.updateBestId(chainId, info.getTime());
    }

}
