package io.icw.transaction.rpc.call;

import io.icw.base.RPCUtil;
import io.icw.base.basic.AddressTool;
import io.icw.core.exception.NulsException;
import io.icw.core.model.BigIntegerUtils;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.model.message.MessageUtil;
import io.icw.core.rpc.model.message.Request;
import io.icw.core.rpc.netty.processor.ResponseMessageProcessor;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.bo.VerifyLedgerResult;
import io.icw.transaction.model.po.TransactionNetPO;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.constant.TxErrorCode;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class LedgerCall {


    /**
     * 验证单个交易的CoinData(外部使用)
     * 有异常直接抛出
     *
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyLedgerResult verifyCoinData(Chain chain, String tx) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", tx);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "verifyCoinData", params);
            return VerifyLedgerResult.success((boolean)result.get("orphan"));
        } catch (NulsException e) {
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLogger().error(e);
            return VerifyLedgerResult.fail(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }



    /**
     * 验证单个交易与未确认交易提交
     * @param chain
     * @param txStr
     */
    public static VerifyLedgerResult commitUnconfirmedTx(Chain chain, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", txStr);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
            Boolean orphan = (Boolean) result.get("orphan");
            if (null == orphan) {
                chain.getLogger().error("call commitUnconfirmedTx response orphan is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return VerifyLedgerResult.fail(TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND);
            }
            return VerifyLedgerResult.success(orphan);
        } catch (NulsException e) {
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLogger().error(e);
            return VerifyLedgerResult.fail(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 新交易验证账本(批量)
     * @param chain
     * @param txNetList
     */
    public static Map commitBatchUnconfirmedTxs(Chain chain,List<TransactionNetPO> txNetList) throws NulsException {

        try {
            List<String> txStrList = new ArrayList<>();
            for(TransactionNetPO txNet : txNetList){
                txStrList.add(RPCUtil.encode(txNet.getTx().serialize()));
            }
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txStrList);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "commitBatchUnconfirmedTxs", params);
            return result;
        }catch (IOException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        }catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }


    /**
     * 批量验证CoinData时的单个发送(不用于单个交易的独立验证)
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyLedgerResult verifyCoinDataPackaged(Chain chain, String tx) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", tx);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "verifyCoinDataPackaged", params);
            Boolean orphan = (Boolean) result.get("orphan");
            if (null == orphan) {
                chain.getLogger().error("call verifyCoinDataPackaged response orphan is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return VerifyLedgerResult.fail(TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND);
            }
            return VerifyLedgerResult.success(orphan);
        } catch (NulsException e) {
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLogger().error(e);
            return VerifyLedgerResult.fail(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 打包验证交易coinData(批量)
     * @param chain
     * @param txStrList
     * @return
     */
    public static Map verifyCoinDataBatchPackaged(Chain chain, List<String> txStrList) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txStrList);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "verifyCoinDataBatchPackaged", params);
            return result;
        }catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 验证区块中的交易CoinData
     * @param chain
     * @param txList
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static boolean verifyBlockTxsCoinData(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "blockValidate", params);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
                chain.getLogger().error("call blockValidate response value is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return false;
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 查询nonce值
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
            String nonce = (String) result.get("nonce");
            if (null == nonce) {
                chain.getLogger().error("call getNonce response nonce is null, error:{}", TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return TxConstant.DEFAULT_NONCE;
            }
            return RPCUtil.decode(nonce);
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 查询账户特定资产的余额(只获取已确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
            Object available = result.get("available");
            if (null == available) {
                chain.getLogger().error("call getBalance response available is null, error:{}", TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return new BigInteger("0");
            }
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 开始批量验证coindata的通知
     * @param chain
     * @return
     * @throws NulsException
     */
    public static boolean coinDataBatchNotify(Chain chain) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "batchValidateBegin", params);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
                chain.getLogger().error("call batchValidateBegin response value is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return false;
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }


    /**
     * 提交已确认交易给账本
     * @param chain
     * @param txList
     */
    public static boolean commitTxsLedger(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "commitBlockTxs", params, TxConstant.TIMEOUT);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
                chain.getLogger().error("call commitBlockTxs response value is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return false;
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 调用账本修改未确认的交易状态
     * @param chain
     * @param txStr
     */
    public static boolean rollbackTxValidateStatus(Chain chain, String txStr) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", txStr);
            Request request = MessageUtil.newRequest("rollbackTxValidateStatus", params, Constants.BOOLEAN_TRUE, Constants.ZERO, Constants.ZERO);
            String messageId = ResponseMessageProcessor.requestOnly(ModuleE.LG.abbr, request);
            return messageId.equals("0") ? false : true;
        } catch (Exception e) {
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 调用账本回滚未确认的交易
     * @param chain
     * @param txStr
     */
    public static boolean rollBackUnconfirmTx(Chain chain, String txStr) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", txStr);
            Request request = MessageUtil.newRequest("rollBackUnconfirmTx", params, Constants.BOOLEAN_TRUE, Constants.ZERO, Constants.ZERO);
            String messageId = ResponseMessageProcessor.requestOnly(ModuleE.LG.abbr, request);
            return messageId.equals("0") ? false : true;
        } catch (Exception e) {
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    /**
     * 调用账本回滚已确认的交易
     * @param chain
     * @param txList
     */
    public static boolean rollbackTxsLedger(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.LG.abbr, "rollBackBlockTxs", params, TxConstant.TIMEOUT);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
                chain.getLogger().error("call rollBackBlockTxs response value is null, error:{}",
                        TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return false;
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    public static void clearUnconfirmTxs(Chain chain) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            TransactionCall.requestAndResponse(ModuleE.LG.abbr, "clearUnconfirmTxs", params);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }


}
