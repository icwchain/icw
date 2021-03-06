package io.icw.block.rpc.call;

import io.icw.base.protocol.ModuleHelper;
import io.icw.block.manager.ContextManager;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.Map;

public class CrossChainCall {

    /**
     * 批量保存交易
     *
     * @param chainId 链Id/chain id
     * @param height
     * @param blockHeader
     * @return
     */
    public static void heightNotice(int chainId, long height, String blockHeader) {
        if (!ModuleHelper.isSupportCrossChain()) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(4);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);
            params.put("blockHeader", blockHeader);
            ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, "newBlockHeight", params);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
