package io.icw.test.rpc.util;

import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.core.log.Log;

import java.util.HashMap;
import java.util.Map;

public class CallUtil {

    public static void importPriKey(int chainId,String priKey, String pwd) {
        try {
            //账户已存在则覆盖 If the account exists, it covers.
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("priKey", priKey);
            params.put("password", pwd);
            params.put("overwrite", true);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_importAccountByPriKey", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_importAccountByPriKey");
            String address = (String) result.get("address");
            Log.debug("importPriKey success! address-{}", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
