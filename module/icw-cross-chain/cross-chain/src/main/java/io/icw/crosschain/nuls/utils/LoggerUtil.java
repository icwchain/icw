package io.icw.crosschain.nuls.utils;

import io.icw.core.rpc.model.ModuleE;
import io.icw.crosschain.nuls.model.bo.Chain;
import io.icw.core.log.logback.LoggerBuilder;
import io.icw.core.log.logback.NulsLogger;

/**
 * 日志管理类
 * Log Management Class
 * @author tag
 * 2019/4/10
 */
public class LoggerUtil {
    private static  String FOLDER_PREFIX = ModuleE.Constant.CROSS_CHAIN;

    /**
     * 跨链模块公用日志类
     * Cross-Chain Module Common Log Class
     * */
    public static NulsLogger commonLog = LoggerBuilder.getLogger(FOLDER_PREFIX);

    /**
     * 初始化某条链的日志信息
     * Initialize log information for a chain
     * @param chain chain info
     * */
    public static void initLogger(Chain chain) {
        int chainId = chain.getConfig().getChainId();
        chain.setLogger(LoggerBuilder.getLogger(ModuleE.Constant.CROSS_CHAIN,chainId));
    }
}
