/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.transaction.rpc.cmd;

import io.icw.base.RPCUtil;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.model.ObjectUtils;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.transaction.constant.TxCmd;
import io.icw.transaction.constant.TxErrorCode;
import io.icw.transaction.model.bo.Chain;
import io.icw.transaction.model.bo.VerifyLedgerResult;
import io.icw.transaction.model.bo.VerifyResult;
import io.icw.transaction.model.po.TransactionConfirmedPO;
import io.icw.transaction.rpc.call.LedgerCall;
import io.icw.core.rpc.model.*;
import io.icw.core.rpc.model.message.Response;
import io.icw.transaction.cache.PackablePool;
import io.icw.transaction.constant.TxConstant;
import io.icw.transaction.manager.ChainManager;
import io.icw.transaction.service.ConfirmedTxService;
import io.icw.transaction.service.TxService;
import io.icw.transaction.storage.UnconfirmedTxStorageService;
import io.icw.transaction.utils.LoggerUtil;
import io.icw.transaction.utils.TxUtil;

import java.util.HashMap;
import java.util.Map;

import static io.icw.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2019/3/12
 */
@Component
public class ClientCmd extends BaseCmd {

    @Autowired
    private TxService txService;

    @Autowired
    private ConfirmedTxService confirmedTxService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private PackablePool packablePool;

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX, version = 1.0, description = "??????hash?????????????????????????????????????????????????????????/Get transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "???????????????hash")
    })
    @ResponseData(name = "?????????", description = "????????????Map?????????????????????key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "????????????????????????????????????????????????"),
            @Key(name = "height", description = "????????????????????????????????????????????????????????????-1"),
            @Key(name = "status", description = "???????????????????????????????????????")
    }))
    public Response getTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = txService.getTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                resultMap.put("tx", null);
            } else {
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
                resultMap.put("height", tx.getBlockHeight());
                resultMap.put("status", tx.getStatus());
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX_CONFIRMED, version = 1.0, description = "??????hash?????????????????????(???????????????)/Get confirmed transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "???????????????hash")
    })
    @ResponseData(name = "?????????", description = "????????????Map?????????????????????key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "????????????????????????????????????????????????"),
            @Key(name = "height", description = "?????????????????????????????????"),
            @Key(name = "status", description = "???????????????????????????????????????")
    }))
    public Response getConfirmedTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = confirmedTxService.getConfirmedTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                LOG.debug("getConfirmedTransaction fail, tx is null. txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                LOG.debug("getConfirmedTransaction success. txHash:{}", txHash);
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
                resultMap.put("height", tx.getBlockHeight());
                resultMap.put("status", tx.getStatus());
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = TxCmd.TX_VERIFYTX, version = 1.0, description = "?????????????????????????????????????????????????????????????????????/Verify transation")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???id"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "??????????????????????????????")
    })
    @ResponseData(name = "?????????", description = "????????????Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "??????hash")
    }))
    public Response verifyTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txStr = (String) params.get("tx");
            //???txStr?????????Transaction??????
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);

            VerifyResult verifyResult = txService.verify(chain, tx);
            if (!verifyResult.getResult()) {
                return failed(verifyResult.getErrorCode());
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.verifyCoinData(chain, RPCUtil.encode(tx.serialize()));
            if (!verifyLedgerResult.getSuccess()) {
                return failed(verifyLedgerResult.getErrorCode());
            }
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("value", tx.getHash().toHex());
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LoggerUtil.LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
