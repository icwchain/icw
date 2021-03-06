package io.icw.account.rpc.call;

import io.icw.account.model.bo.Chain;
import io.icw.account.constant.AccountConstant;
import io.icw.account.constant.AccountErrorCode;
import io.icw.account.constant.RpcConstant;
import io.icw.base.RPCUtil;
import io.icw.base.data.Transaction;
import io.icw.core.constant.ErrorCode;
import io.icw.core.exception.NulsException;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description: 交易模块接口调用
 * @date: 2018/11/27
 */
public class TransactionCall {

    /**
     * 发起新交易
     */
    public static boolean newTx(Chain chain, Transaction tx) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chain.getChainId());
            Response cmdResp = null;
            try {
                params.put(RpcConstant.TX_DATA, RPCUtil.encode(tx.serialize()));
                cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstant.TX_NEW_CMD, params);
            } catch (IOException e) {
                chain.getLogger().error(e);
                throw new NulsException(AccountErrorCode.SERIALIZE_ERROR);
            } catch (Exception e) {
                chain.getLogger().error(e);
                throw new NulsException(AccountErrorCode.RPC_REQUEST_FAILD);
            }
            if (!cmdResp.isSuccess()) {
                String errorCode = cmdResp.getResponseErrorCode();
                chain.getLogger().error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{} hash:{}",
                        RpcConstant.TX_NEW_CMD, errorCode, cmdResp.getResponseComment(), tx.getHash().toHex());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            return cmdResp.isSuccess();
        } catch (RuntimeException e) {
            throw new NulsException(AccountErrorCode.RPC_REQUEST_FAILD);
        }

    }

}
