package io.icw.api.task;

import io.icw.api.analysis.WalletRpcHandler;
import io.icw.api.ApiContext;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.protocol.ProtocolProvider;
import io.icw.base.api.provider.protocol.facade.GetVersionReq;
import io.icw.base.api.provider.protocol.facade.VersionInfo;
import io.icw.core.basic.Result;
import io.icw.core.log.Log;

import java.util.Map;

public class GetGlobalInfoTask implements Runnable {

    private int chainId;

    public GetGlobalInfoTask(int chainId) {
        this.chainId = chainId;
    }

    private ProtocolProvider transferService;

    @Override
    public void run() {
        try{
            Result<Map<String, Object>> result = WalletRpcHandler.getBlockGlobalInfo(chainId);
            if (result == null || result.isFailed()) {
                Log.error("----------GetGlobalInfoTask getBlockGlobalInfo error----------");
                return;
            }
            Map<String, Object> map = result.getData();
            ApiContext.localHeight = Long.parseLong(map.get("localHeight").toString());
            ApiContext.networkHeight = Long.parseLong(map.get("networkHeight").toString());

            if (ApiContext.magicNumber == 0) {
                result = WalletRpcHandler.getNetworkInfo(chainId);
                map = result.getData();
                ApiContext.magicNumber = Integer.parseInt(map.get("magicNumber").toString());
            }

            transferService = ServiceManager.get(ProtocolProvider.class);
            io.icw.base.api.provider.Result<VersionInfo> res = transferService.getVersion(new GetVersionReq());
            if (res.isSuccess()) {
                VersionInfo info = res.getData();
                ApiContext.localProtocolVersion = info.getLocalProtocolVersion();
            } else {
                Log.error("----------GetGlobalInfoTask getVersion fail----------" + res.getMessage());
            }
        }catch (Exception e) {
            Log.error("----------GetGlobalInfoTask error----------");
            Log.error(e);
        }

    }
}
