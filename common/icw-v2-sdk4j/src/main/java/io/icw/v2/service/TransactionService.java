package io.icw.v2.service;

import io.icw.v2.model.dto.*;
import io.icw.v2.txdata.*;
import io.icw.v2.util.*;
import io.icw.base.basic.AddressTool;
import io.icw.base.data.*;
import io.icw.base.signture.MultiSignTxSignature;
import io.icw.base.signture.P2PHKSignature;
import io.icw.core.basic.Result;
import io.icw.core.constant.ErrorCode;
import io.icw.core.constant.TxType;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.model.StringUtils;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.v2.SDKContext;
import io.icw.v2.constant.AccountConstant;
import io.icw.v2.error.AccountErrorCode;
import io.icw.v2.model.dto.*;
import io.icw.v2.txdata.*;
import io.icw.v2.util.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.icw.v2.SDKContext.NULS_DEFAULT_OTHER_TX_FEE_PRICE;
import static io.icw.v2.constant.AccountConstant.ALIAS_FEE;
import static io.icw.v2.constant.Constant.NULS_CHAIN_ID;
import static io.icw.v2.util.ValidateUtil.validateChainId;

public class TransactionService {

    private TransactionService() {

    }

    private static TransactionService instance = new TransactionService();

    public static TransactionService getInstance() {
        return instance;
    }

    public Result getTx(String txHash) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/tx/" + txHash);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            TransactionDto tx = TransactionDto.mapToPojo(map);
            result.setData(tx);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getTransaction(String txHash) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/tx/" + txHash);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            TransactionDto tx = TransactionDto.mapToPojo(map);
            restFulResult = RestFulUtil.get("api/block/header/height/" + tx.getBlockHeight());
            if (restFulResult.isSuccess()) {
                map = (Map<String, Object>) restFulResult.getData();
                tx.setBlockHash((String) map.get("hash"));
            }
            result.setData(tx);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result transfer(TransferForm transferForm) {
        validateChainId();

        Map<String, Object> params = new HashMap<>();
        params.put("address", transferForm.getAddress());
        params.put("toAddress", transferForm.getToAddress());
        params.put("password", transferForm.getPassword());
        params.put("amount", transferForm.getAmount());
        params.put("remark", transferForm.getRemark());
        RestFulResult restFulResult = RestFulUtil.post("api/accountledger/transfer", params);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(restFulResult.getData());
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result crossTransfer(CrossTransferForm form) {
        validateChainId();
        Map<String, Object> params = new HashMap<>();
        params.put("address", form.getAddress());
        params.put("toAddress", form.getToAddress());
        params.put("password", form.getPassword());
        params.put("assetChainId", form.getAssetChainId());
        params.put("assetId", form.getAssetId());
        params.put("amount", form.getAmount());
        params.put("remark", form.getRemark());

        RestFulResult restFulResult = RestFulUtil.post("api/accountledger/crossTransfer", params);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(restFulResult.getData());
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    /**
     * ???????????????????????????
     *
     * @param dto ????????????
     * @return result
     */
    public BigInteger calcTransferTxFee(TransferTxFeeDto dto) {
        if (dto.getPrice() == null) {
            dto.setPrice(SDKContext.NULS_DEFAULT_NORMAL_TX_FEE_PRICE);
        }
        return TxUtils.calcTransferTxFee(dto.getAddressCount(), dto.getFromLength(), dto.getToLength(), dto.getRemark(), dto.getPrice());
    }


    @Deprecated
    public Map<String, BigInteger> calcCrossTransferTxFee(CrossTransferTxFeeDto dto) {
        boolean isMainNet = false;
        if (SDKContext.main_chain_id == NULS_CHAIN_ID || SDKContext.main_chain_id == 2) {
            isMainNet = true;
        }
        return TxUtils.calcCrossTxFee(dto.getAddressCount(), dto.getFromLength(), dto.getToLength(), dto.getRemark(), isMainNet);
    }

    /**
     * ??????NULS??????????????????????????????NULS?????????
     *
     * @param dto
     * @return
     */
    public BigInteger calcCrossTransferNulsTxFee(CrossTransferTxFeeDto dto) {
        return TxUtils.calcCrossTxFee(dto.getAddressCount(), dto.getFromLength(), dto.getToLength(), dto.getRemark());
    }

    /**
     * ????????? ?????????NULS??????????????????NULS??????????????????????????????????????????(???????????????NULS)???
     * ?????????????????????fromAddress?????????NULS???????????????????????????
     * ??????from????????????????????????????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress ???????????????NULS?????????
     * @param toAddress   ???????????????NULS?????????
     * @param assetId
     * @param amount
     * @return
     */
    public Result createTxSimpleTransferOfNonNuls(String fromAddress, String toAddress, int assetChainId, int assetId, BigInteger amount) {
        return createTxSimpleTransferOfNonNuls(fromAddress, toAddress, assetChainId, assetId, amount, 0, null);
    }

    /**
     * ????????? ?????????NULS??????????????????NULS??????????????????????????????????????????(???????????????NULS)???
     * ?????????????????????fromAddress?????????NULS???????????????????????????
     * ??????from????????????????????????????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress  ???????????????NULS?????????
     * @param toAddress    ???????????????NULS?????????
     * @param assetChainId
     * @param assetId
     * @param amount
     * @param time
     * @param remark
     * @return
     */
    public Result createTxSimpleTransferOfNonNuls(String fromAddress, String toAddress, int assetChainId, int assetId, BigInteger amount, long time, String remark) {
        Result accountBalanceR = NulsSDKTool.getAccountBalance(fromAddress, assetChainId, assetId);
        if (!accountBalanceR.isSuccess()) {
            return Result.getFailed(accountBalanceR.getErrorCode()).setMsg(accountBalanceR.getMsg());
        }
        Map balance = (Map) accountBalanceR.getData();
        BigInteger senderBalance = new BigInteger(balance.get("available").toString());
        if (senderBalance.compareTo(amount) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
        }
        String nonce = balance.get("nonce").toString();

        TransferDto transferDto = new TransferDto();
        List<CoinFromDto> inputs = new ArrayList<>();

        //????????????
        CoinFromDto from = new CoinFromDto();
        from.setAddress(fromAddress);
        from.setAmount(amount);
        from.setAssetChainId(assetChainId);
        from.setAssetId(assetId);
        from.setNonce(nonce);
        inputs.add(from);

        Result accountBalanceFeeR = NulsSDKTool.getAccountBalance(fromAddress, SDKContext.main_chain_id, SDKContext.main_asset_id);
        if (!accountBalanceFeeR.isSuccess()) {
            return Result.getFailed(accountBalanceFeeR.getErrorCode()).setMsg(accountBalanceFeeR.getMsg());
        }
        Map balanceFee = (Map) accountBalanceFeeR.getData();
        BigInteger senderBalanceFee = new BigInteger(balanceFee.get("available").toString());

        TransferTxFeeDto feeDto = new TransferTxFeeDto();
        feeDto.setAddressCount(1);
        feeDto.setFromLength(2);
        feeDto.setToLength(1);
        feeDto.setRemark(remark);
        BigInteger feeNeed = NulsSDKTool.calcTransferTxFee(feeDto);
        if (senderBalanceFee.compareTo(feeNeed) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_FEE);
        }
        String nonceFee = balanceFee.get("nonce").toString();
        //???????????????
        CoinFromDto fromFee = new CoinFromDto();
        fromFee.setAddress(fromAddress);
        fromFee.setAmount(feeNeed);
        fromFee.setAssetChainId(SDKContext.main_chain_id);
        fromFee.setAssetId(SDKContext.main_asset_id);
        fromFee.setNonce(nonceFee);
        inputs.add(fromFee);

        List<CoinToDto> outputs = new ArrayList<>();
        CoinToDto to = new CoinToDto();
        to.setAddress(toAddress);
        to.setAmount(amount);
        to.setAssetChainId(assetChainId);
        to.setAssetId(assetId);
        outputs.add(to);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        transferDto.setTime(time);
        transferDto.setRemark(remark);
        return createTransferTx(transferDto);
    }


    /**
     * ????????? ?????????NULS???????????????NULS?????????????????????????????????????????????
     * !! ???????????????????????????amount?????? ???????????????fromAddress?????????????????????????????????coinfrom??????
     * ????????????????????????????????????amount???????????? amount???????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     */
    public Result createTxSimpleTransferOfNuls(String fromAddress, String toAddress, BigInteger amount) {
        return createTxSimpleTransferOfNuls(fromAddress, toAddress, amount, 0, null);
    }

    /**
     * ????????? ?????????NULS???????????????NULS?????????????????????????????????????????????
     * !! ???????????????????????????amount?????? ???????????????fromAddress?????????????????????????????????coinfrom??????
     * ????????????????????????????????????amount???????????? amount????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @param time
     * @param remark
     * @return
     */
    public Result createTxSimpleTransferOfNuls(String fromAddress, String toAddress, BigInteger amount, long time, String remark) {
        Result accountBalanceR = NulsSDKTool.getAccountBalance(fromAddress, SDKContext.main_chain_id, SDKContext.main_asset_id);
        if (!accountBalanceR.isSuccess()) {
            return Result.getFailed(accountBalanceR.getErrorCode()).setMsg(accountBalanceR.getMsg());
        }
        Map balance = (Map) accountBalanceR.getData();
        BigInteger senderBalance = new BigInteger(balance.get("available").toString());

        TransferTxFeeDto feeDto = new TransferTxFeeDto();
        feeDto.setAddressCount(1);
        feeDto.setFromLength(2);
        feeDto.setToLength(1);
        feeDto.setRemark(remark);
        BigInteger feeNeed = NulsSDKTool.calcTransferTxFee(feeDto);
        BigInteger amountTotal = amount.add(feeNeed);
        if (senderBalance.compareTo(amountTotal) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
        }
        String nonce = balance.get("nonce").toString();

        TransferDto transferDto = new TransferDto();
        List<CoinFromDto> inputs = new ArrayList<>();

        //????????????
        CoinFromDto from = new CoinFromDto();
        from.setAddress(fromAddress);
        from.setAmount(amountTotal);
        from.setAssetChainId(SDKContext.main_chain_id);
        from.setAssetId(SDKContext.main_asset_id);
        from.setNonce(nonce);
        inputs.add(from);

        List<CoinToDto> outputs = new ArrayList<>();
        CoinToDto to = new CoinToDto();
        to.setAddress(toAddress);
        to.setAmount(amount);
        to.setAssetChainId(SDKContext.main_chain_id);
        to.setAssetId(SDKContext.main_asset_id);
        outputs.add(to);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        transferDto.setTime(time);
        transferDto.setRemark(remark);
        return createTransferTx(transferDto);
    }

    /**
     * ??????????????????(??????)
     * create transfer transaction(off-line)
     *
     * @param transferDto ??????????????????
     * @return
     */
    public Result createTransferTx(TransferDto transferDto) {
        validateChainId();
        try {
            CommonValidator.checkTransferDto(transferDto);

            for (CoinFromDto fromDto : transferDto.getInputs()) {
                if (fromDto.getAssetChainId() == 0) {
                    fromDto.setAssetChainId(SDKContext.main_chain_id);
                }
                if (fromDto.getAssetId() == 0) {
                    fromDto.setAssetId(SDKContext.main_asset_id);
                }
            }
            for (CoinToDto toDto : transferDto.getOutputs()) {
                if (toDto.getAssetChainId() == 0) {
                    toDto.setAssetChainId(SDKContext.main_chain_id);
                }
                if (toDto.getAssetId() == 0) {
                    toDto.setAssetId(SDKContext.main_asset_id);
                }
            }

            Transaction tx = new Transaction(TxType.TRANSFER);
            if (transferDto.getTime() != 0) {
                tx.setTime(transferDto.getTime());
            } else {
                tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            }
            tx.setRemark(StringUtils.bytes(transferDto.getRemark()));

            CoinData coinData = assemblyCoinData(transferDto.getInputs(), transferDto.getOutputs(), tx.getSize());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    /**
     * ?????????????????????coinData??????
     * Assemble the coinData for the transfer transaction
     *
     * @return coinData
     * @throws NulsException
     */
    private CoinData assemblyCoinData(List<CoinFromDto> inputs, List<CoinToDto> outputs, int txSize) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinFromDto from : inputs) {
            byte[] address = AddressTool.getAddress(from.getAddress());
            byte[] nonce = HexUtil.decode(from.getNonce());
            CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), nonce, AccountConstant.NORMAL_TX_LOCKED);
            coinFroms.add(coinFrom);
        }

        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinToDto to : outputs) {
            byte[] addressByte = AddressTool.getAddress(to.getAddress());
            CoinTo coinTo = new CoinTo(addressByte, to.getAssetChainId(), to.getAssetId(), to.getAmount(), to.getLockTime());
            coinTos.add(coinTo);
        }

        txSize = txSize + getSignatureSize(coinFroms);
        TxUtils.calcTxFee(coinFroms, coinTos, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    /**
     * ????????? ?????????????????????NULS????????????????????????????????????????????????(???????????????NULS)???
     * ?????????????????????fromAddress?????????NULS???????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress  ???????????????NULS?????????
     * @param toAddress    ??????????????????NULS?????????
     * @param assetChainId ???????????????id
     * @param assetId      ????????????id
     * @param amount       ??????token??????
     * @return
     */
    public Result createCrossTxSimpleTransferOfNonNuls(String fromAddress, String toAddress, int assetChainId, int assetId, BigInteger amount) {
        return createCrossTxSimpleTransferOfNonNuls(fromAddress, toAddress, assetChainId, assetId, amount, 0, null);
    }

    /**
     * ????????? ?????????????????????NULS????????????????????????????????????????????????(???????????????NULS)???
     * ?????????????????????fromAddress?????????NULS???????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress  ???????????????NULS?????????
     * @param toAddress    ??????????????????NULS?????????
     * @param assetChainId ???????????????id
     * @param assetId      ????????????id
     * @param amount       ??????token??????
     * @param time         ????????????
     * @param remark       ??????
     * @return
     */
    public Result createCrossTxSimpleTransferOfNonNuls(String fromAddress, String toAddress, int assetChainId, int assetId, BigInteger amount, long time, String remark) {
        Result accountBalanceR = NulsSDKTool.getAccountBalance(fromAddress, assetChainId, assetId);
        if (!accountBalanceR.isSuccess()) {
            return Result.getFailed(accountBalanceR.getErrorCode()).setMsg(accountBalanceR.getMsg());
        }
        Map balance = (Map) accountBalanceR.getData();
        BigInteger senderBalance = new BigInteger(balance.get("available").toString());
        if (senderBalance.compareTo(amount) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
        }
        String nonce = balance.get("nonce").toString();

        TransferDto transferDto = new TransferDto();
        List<CoinFromDto> inputs = new ArrayList<>();

        //????????????
        CoinFromDto from = new CoinFromDto();
        from.setAddress(fromAddress);
        from.setAmount(amount);
        from.setAssetChainId(assetChainId);
        from.setAssetId(assetId);
        from.setNonce(nonce);
        inputs.add(from);

        Result accountBalanceFeeR = NulsSDKTool.getAccountBalance(fromAddress, SDKContext.main_chain_id, SDKContext.main_asset_id);
        if (!accountBalanceFeeR.isSuccess()) {
            return Result.getFailed(accountBalanceFeeR.getErrorCode()).setMsg(accountBalanceFeeR.getMsg());
        }
        Map balanceFee = (Map) accountBalanceFeeR.getData();
        BigInteger senderBalanceFee = new BigInteger(balanceFee.get("available").toString());

        CrossTransferTxFeeDto crossFeeDto = new CrossTransferTxFeeDto();
        crossFeeDto.setAddressCount(1);
        crossFeeDto.setFromLength(2);
        crossFeeDto.setToLength(1);
        crossFeeDto.setRemark(remark);
        BigInteger feeNeed = calcCrossTransferNulsTxFee(crossFeeDto);
        if (senderBalanceFee.compareTo(feeNeed) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_FEE);
        }
        String nonceFee = balanceFee.get("nonce").toString();
        //???????????????
        CoinFromDto fromFee = new CoinFromDto();
        fromFee.setAddress(fromAddress);
        fromFee.setAmount(feeNeed);
        fromFee.setAssetChainId(SDKContext.main_chain_id);
        fromFee.setAssetId(SDKContext.main_asset_id);
        fromFee.setNonce(nonceFee);
        inputs.add(fromFee);

        List<CoinToDto> outputs = new ArrayList<>();
        CoinToDto to = new CoinToDto();
        to.setAddress(toAddress);
        to.setAmount(amount);
        to.setAssetChainId(assetChainId);
        to.setAssetId(assetId);
        outputs.add(to);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        transferDto.setTime(time);
        transferDto.setRemark(remark);
        return createCrossTransferTx(transferDto);
    }


    /**
     * ????????? ??????????????????NULS???????????????????????????????????????????????????
     * !! ???????????????????????????amount?????? ???????????????fromAddress?????????????????????????????????coinfrom??????
     * ????????????????????????????????????amount???????????? amount????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress
     * @param toAddress ??????NULS?????????
     * @param amount
     * @return
     */
    public Result createCrossTxSimpleTransferOfNuls(String fromAddress, String toAddress, BigInteger amount) {
        return createCrossTxSimpleTransferOfNuls(fromAddress, toAddress, amount, 0, null);
    }

    /**
     * ????????? ??????????????????NULS???????????????????????????????????????????????????
     * !! ???????????????????????????amount?????? ???????????????fromAddress?????????????????????????????????coinfrom??????
     * ????????????????????????????????????amount???????????? amount????????????????????????????????????
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param fromAddress
     * @param toAddress ??????NULS?????????
     * @param amount
     * @param time
     * @param remark
     * @return
     */
    public Result createCrossTxSimpleTransferOfNuls(String fromAddress, String toAddress, BigInteger amount, long time, String remark) {
        Result accountBalanceR = NulsSDKTool.getAccountBalance(fromAddress, SDKContext.main_chain_id, SDKContext.main_asset_id);
        if (!accountBalanceR.isSuccess()) {
            return Result.getFailed(accountBalanceR.getErrorCode()).setMsg(accountBalanceR.getMsg());
        }
        Map balance = (Map) accountBalanceR.getData();
        BigInteger senderBalance = new BigInteger(balance.get("available").toString());

        CrossTransferTxFeeDto crossFeeDto = new CrossTransferTxFeeDto();
        crossFeeDto.setAddressCount(1);
        crossFeeDto.setFromLength(2);
        crossFeeDto.setToLength(1);
        crossFeeDto.setRemark(remark);
        BigInteger feeNeed = NulsSDKTool.calcCrossTransferNulsTxFee(crossFeeDto);
        BigInteger amountTotal = amount.add(feeNeed);
        if (senderBalance.compareTo(amountTotal) < 0) {
            return Result.getFailed(AccountErrorCode.INSUFFICIENT_BALANCE);
        }
        String nonce = balance.get("nonce").toString();

        TransferDto transferDto = new TransferDto();
        List<CoinFromDto> inputs = new ArrayList<>();

        //????????????
        CoinFromDto from = new CoinFromDto();
        from.setAddress(fromAddress);
        from.setAmount(amountTotal);
        from.setAssetChainId(SDKContext.main_chain_id);
        from.setAssetId(SDKContext.main_asset_id);
        from.setNonce(nonce);
        inputs.add(from);

        List<CoinToDto> outputs = new ArrayList<>();
        CoinToDto to = new CoinToDto();
        to.setAddress(toAddress);
        to.setAmount(amount);
        to.setAssetChainId(SDKContext.main_chain_id);
        to.setAssetId(SDKContext.main_asset_id);
        outputs.add(to);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        transferDto.setTime(time);
        transferDto.setRemark(remark);
        return createCrossTransferTx(transferDto);
    }


    /**
     * ????????????????????????
     *
     * @param transferDto
     * @return
     */
    public Result createCrossTransferTx(TransferDto transferDto) {
        validateChainId();
        try {
            CommonValidator.checkCrossTransferDto(transferDto);
            Transaction tx = new Transaction(TxType.CROSS_CHAIN);
            if (transferDto.getTime() != 0) {
                tx.setTime(transferDto.getTime());
            } else {
                tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            }
            tx.setRemark(StringUtils.bytes(transferDto.getRemark()));

            CoinData coinData = createCrossTxCoinData(transferDto.getInputs(), transferDto.getOutputs());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));

            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }

    }

    public CoinData createCrossTxCoinData(List<CoinFromDto> inputs, List<CoinToDto> outputs) {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinFromDto from : inputs) {
            byte[] address = AddressTool.getAddress(from.getAddress());
            byte[] nonce = HexUtil.decode(from.getNonce());
            CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), nonce, AccountConstant.NORMAL_TX_LOCKED);
            coinFroms.add(coinFrom);
        }

        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinToDto to : outputs) {
            byte[] addressByte = AddressTool.getAddress(to.getAddress());
            CoinTo coinTo = new CoinTo(addressByte, to.getAssetChainId(), to.getAssetId(), to.getAmount(), to.getLockTime());
            coinTos.add(coinTo);
        }

        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);

        return coinData;
    }


    public Result createMultiSignTransferTx(MultiSignTransferDto transferDto) {
        validateChainId();
        try {
            CommonValidator.checkMultiSignTransferDto(transferDto);
            for (CoinFromDto fromDto : transferDto.getInputs()) {
                if (fromDto.getAssetChainId() == 0) {
                    fromDto.setAssetChainId(SDKContext.main_chain_id);
                }
                if (fromDto.getAssetId() == 0) {
                    fromDto.setAssetId(SDKContext.main_asset_id);
                }
            }
            for (CoinToDto toDto : transferDto.getOutputs()) {
                if (toDto.getAssetChainId() == 0) {
                    toDto.setAssetChainId(SDKContext.main_chain_id);
                }
                if (toDto.getAssetId() == 0) {
                    toDto.setAssetId(SDKContext.main_asset_id);
                }
            }

            Transaction tx = new Transaction(TxType.TRANSFER);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            tx.setRemark(StringUtils.bytes(transferDto.getRemark()));

            CoinData coinData = assemblyCoinData(transferDto, tx.getSize());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) transferDto.getMinSigns());

            List<byte[]> list = new ArrayList<>();
            for (String pubKey : transferDto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    private CoinData assemblyCoinData(MultiSignTransferDto transferDto, int txSize) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinFromDto from : transferDto.getInputs()) {
            byte[] address = AddressTool.getAddress(from.getAddress());
            byte[] nonce = HexUtil.decode(from.getNonce());
            CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), nonce, AccountConstant.NORMAL_TX_LOCKED);
            coinFroms.add(coinFrom);
        }

        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinToDto to : transferDto.getOutputs()) {
            byte[] addressByte = AddressTool.getAddress(to.getAddress());
            CoinTo coinTo = new CoinTo(addressByte, to.getAssetChainId(), to.getAssetId(), to.getAmount(), to.getLockTime());
            coinTos.add(coinTo);
        }

        txSize = txSize + getMultiSignSignatureSize(transferDto.getPubKeys().size());
        TxUtils.calcTxFee(coinFroms, coinTos, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    /**
     * ???????????????????????????
     *
     * @param dto ????????????
     * @return result
     */
    public BigInteger calcMultiSignTransferTxFee(MultiSignTransferTxFeeDto dto) {
        if (dto.getPrice() == null) {
            dto.setPrice(SDKContext.NULS_DEFAULT_NORMAL_TX_FEE_PRICE);
        }
        return TxUtils.calcTransferTxFee(dto.getPubKeyCount(), dto.getFromLength(), dto.getToLength(), dto.getRemark(), dto.getPrice());
    }

    /**
     * ??????coinFroms?????????????????????size
     * ??????coinFroms?????????????????????????????????
     * Calculate the size of the signature data by coinFroms
     * if coinFroms has duplicate addresses, it will only be evaluated once
     *
     * @param coinFroms ????????????
     * @return int size
     */
    private int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> commonAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            commonAddress.add(address);
        }
        size += commonAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    private int getMultiSignSignatureSize(int signNumber) {
        int size = signNumber * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    public Result createAliasTx(AliasDto aliasDto) {
        validateChainId();
        try {
            CommonValidator.checkAliasDto(aliasDto);

            Transaction tx = new Transaction(TxType.ACCOUNT_ALIAS);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            tx.setRemark(StringUtils.bytes(aliasDto.getRemark()));

            Alias alias = new Alias(AddressTool.getAddress(aliasDto.getAddress()), aliasDto.getAlias());
            tx.setTxData(alias.serialize());

            CoinData coinData = assemblyCoinData(aliasDto);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    private CoinData assemblyCoinData(AliasDto dto) {
        byte[] address = AddressTool.getAddress(dto.getAddress());
        byte[] nonce = HexUtil.decode(dto.getNonce());

        List<CoinFrom> coinFroms = new ArrayList<>();
        CoinFrom coinFrom = new CoinFrom();
        coinFrom.setAddress(address);
        coinFrom.setNonce(nonce);
        coinFrom.setAmount(ALIAS_FEE.add(NULS_DEFAULT_OTHER_TX_FEE_PRICE));
        coinFrom.setAssetsChainId(SDKContext.main_chain_id);
        coinFrom.setAssetsId(SDKContext.main_asset_id);
        coinFroms.add(coinFrom);

        String prefix = AccountTool.getPrefix(dto.getAddress());
        List<CoinTo> coinTos = new ArrayList<>();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(AccountConstant.DESTORY_PUBKEY, SDKContext.main_chain_id, prefix));
        coinTo.setAmount(ALIAS_FEE);
        coinTo.setAssetsChainId(SDKContext.main_chain_id);
        coinTo.setAssetsId(SDKContext.main_asset_id);
        coinTos.add(coinTo);

        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    /**
     * ??????????????????????????????
     * Assemble to create consensus node transactions
     *
     * @param consensusDto ??????????????????????????????
     * @return result
     */
    public Result createConsensusTx(ConsensusDto consensusDto) {
        validateChainId();
        try {
            if (StringUtils.isBlank(consensusDto.getRewardAddress())) {
                consensusDto.setRewardAddress(consensusDto.getAgentAddress());
            }
            CommonValidator.validateConsensusDto(consensusDto);

            if (consensusDto.getInput().getAssetChainId() == 0) {
                consensusDto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (consensusDto.getInput().getAssetId() == 0) {
                consensusDto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.REGISTER_AGENT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            Agent agent = new Agent();
            agent.setAgentAddress(AddressTool.getAddress(consensusDto.getAgentAddress()));
            agent.setPackingAddress(AddressTool.getAddress(consensusDto.getPackingAddress()));
            agent.setRewardAddress(AddressTool.getAddress(consensusDto.getRewardAddress()));
            agent.setDeposit((consensusDto.getDeposit()));
            agent.setCommissionRate((byte) consensusDto.getCommissionRate());
            tx.setTxData(agent.serialize());

            CoinData coinData = assemblyCoinData(consensusDto.getInput(), agent.getDeposit(), tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    /**
     * Create a proxy consensus transaction
     * ????????????????????????
     *
     * @param dto ????????????????????????
     * @return result
     */
    public Result createDepositTx(DepositDto dto) {
        validateChainId();
        try {
            CommonValidator.validateDepositDto(dto);
            if (dto.getInput().getAssetChainId() == 0) {
                dto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (dto.getInput().getAssetId() == 0) {
                dto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.DEPOSIT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            Deposit deposit = new Deposit();
            deposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            deposit.setAgentHash(NulsHash.fromHex(dto.getAgentHash()));
            deposit.setDeposit(dto.getDeposit());
            tx.setTxData(deposit.serialize());

            CoinData coinData = assemblyCoinData(dto.getInput(), dto.getDeposit(), tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    private CoinData assemblyCoinData(CoinFromDto from, BigInteger amount, int txSize) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();

        byte[] address = AddressTool.getAddress(from.getAddress());
        byte[] nonce = HexUtil.decode(from.getNonce());
        CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), nonce, AccountConstant.NORMAL_TX_LOCKED);
        coinFroms.add(coinFrom);

        List<CoinTo> coinTos = new ArrayList<>();
        CoinTo coinTo = new CoinTo(address, from.getAssetChainId(), from.getAssetId(), amount, -1);
        coinTos.add(coinTo);

        txSize = txSize + getSignatureSize(coinFroms);
        TxUtils.calcTxFee(coinFroms, coinTos, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    /**
     * ????????????????????????
     *
     * @param dto ????????????????????????
     * @return result
     */
    public Result createWithdrawDepositTx(WithDrawDto dto) {
        validateChainId();

        try {
            if (dto.getPrice() == null) {
                dto.setPrice(NULS_DEFAULT_OTHER_TX_FEE_PRICE);
            }
            CommonValidator.validateWithDrawDto(dto);
            if (dto.getInput().getAssetChainId() == 0) {
                dto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (dto.getInput().getAssetId() == 0) {
                dto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.CANCEL_DEPOSIT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            cancelDeposit.setJoinTxHash(NulsHash.fromHex(dto.getDepositHash()));
            tx.setTxData(cancelDeposit.serialize());

            CoinData coinData = assemblyCoinData(dto, tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);

        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    /**
     * ????????????????????????coinData
     *
     * @param dto    ????????????
     * @param txSize ????????????
     * @return coinData
     * @throws NulsException
     */
    private CoinData assemblyCoinData(WithDrawDto dto, int txSize) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();

        CoinFromDto from = dto.getInput();
        byte[] address = AddressTool.getAddress(from.getAddress());
        CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), (byte) -1);
        NulsHash nulsHash = NulsHash.fromHex(dto.getDepositHash());
        coinFrom.setNonce(TxUtils.getNonce(nulsHash.getBytes()));
        coinFroms.add(coinFrom);

        List<CoinTo> coinTos = new ArrayList<>();
        CoinTo coinTo = new CoinTo(address, from.getAssetChainId(), from.getAssetId(), from.getAmount().subtract(dto.getPrice()), 0);
        coinTos.add(coinTo);

        txSize = txSize + getSignatureSize(coinFroms);
        TxUtils.calcTxFee(coinFroms, coinTos, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    /**
     * ??????????????????????????????
     *
     * @param dto ????????????????????????
     * @return result
     */
    public Result createStopConsensusTx(StopConsensusDto dto) {
        validateChainId();

        try {
            if (dto.getPrice() == null) {
                dto.setPrice(NULS_DEFAULT_OTHER_TX_FEE_PRICE);
            }
            CommonValidator.validateStopConsensusDto(dto);
            for (StopDepositDto depositDto : dto.getDepositList()) {
                if (depositDto.getInput().getAssetChainId() == 0) {
                    depositDto.getInput().setAssetChainId(SDKContext.main_chain_id);
                }
                if (depositDto.getInput().getAssetId() == 0) {
                    depositDto.getInput().setAssetId(SDKContext.main_asset_id);
                }
            }

            Transaction tx = new Transaction(TxType.STOP_AGENT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            StopAgent stopAgent = new StopAgent();
            NulsHash nulsHash = NulsHash.fromHex(dto.getAgentHash());
            stopAgent.setCreateTxHash(nulsHash);
            tx.setTxData(stopAgent.serialize());

            CoinData coinData = assemblyCoinData(dto, tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    /**
     * ????????????????????????coinData
     *
     * @param dto    ??????
     * @param txSize ????????????
     * @return
     * @throws NulsException
     */
    private CoinData assemblyCoinData(StopConsensusDto dto, int txSize) throws NulsException {
        //????????????????????????????????????chainId???assetId
        int chainId = SDKContext.main_chain_id;
        int assetId = SDKContext.main_asset_id;

        List<CoinFrom> coinFromList = new ArrayList<>();
        //???????????????????????????coinFrom
        byte[] addressBytes = AddressTool.getAddress(dto.getAgentAddress());
        CoinFrom coinFrom = new CoinFrom(addressBytes, chainId, assetId, dto.getDeposit(), (byte) -1);
        NulsHash nulsHash = NulsHash.fromHex(dto.getAgentHash());
        coinFrom.setNonce(TxUtils.getNonce(nulsHash.getBytes()));
        coinFromList.add(coinFrom);

        Map<String, CoinFromDto> dtoMap = new HashMap<>();
        CoinFromDto fromDto;
        //?????????????????????coinFrom
        for (StopDepositDto depositDto : dto.getDepositList()) {
            CoinFromDto input = depositDto.getInput();
            byte[] address = AddressTool.getAddress(input.getAddress());
            CoinFrom coinFrom1 = new CoinFrom(address, input.getAssetChainId(), input.getAssetId(), input.getAmount(), (byte) -1);
            NulsHash nulsHash1 = NulsHash.fromHex(depositDto.getDepositHash());
            coinFrom1.setNonce(TxUtils.getNonce(nulsHash1.getBytes()));
            coinFromList.add(coinFrom1);
            //??????????????????????????????????????????????????????
            String key = input.getAddress() + input.getAssetChainId() + input.getAssetId();
            fromDto = dtoMap.get(key);
            if (fromDto == null) {
                dtoMap.put(key, input);
            } else {
                fromDto.setAmount(fromDto.getAmount().add(input.getAmount()));
            }
        }
        //??????dtoMap??????????????????
        List<CoinTo> coinToList = new ArrayList<>();
        for (CoinFromDto input : dtoMap.values()) {
            byte[] address = AddressTool.getAddress(input.getAddress());
            CoinTo coinTo = new CoinTo(address, input.getAssetChainId(), input.getAssetId(), input.getAmount(), 0L);
            coinToList.add(coinTo);
        }
        //???????????????
        BigInteger fee = TxUtils.calcStopConsensusTxFee(coinFromList.size(), coinToList.size() + 1, dto.getPrice());
        //????????????????????????coinTo
        CoinTo coinTo = new CoinTo(addressBytes, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), coinFrom.getAmount().subtract(fee), NulsDateUtils.getCurrentTimeSeconds() + SDKContext.STOP_AGENT_LOCK_TIME);
        coinToList.add(0, coinTo);

        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        TxUtils.calcTxFee(coinFromList, coinToList, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFromList);
        coinData.setTo(coinToList);
        return coinData;
    }

    /**
     * ????????????????????????(??????)
     *
     * @param address
     * @param txHex
     * @return
     */
    public Result signTx(String txHex, String address, String encryptedPrivateKey, String password) {
        List<SignDto> signDtoList = new ArrayList<>();
        SignDto signDto = new SignDto();
        signDto.setAddress(address);
        signDto.setEncryptedPrivateKey(encryptedPrivateKey);
        signDto.setPassword(password);
        signDtoList.add(signDto);
        return NulsSDKTool.sign(signDtoList, txHex);
    }

    /**
     * ????????????????????????(??????)
     *
     * @param address
     * @param txHex
     * @return
     */
    public Result signTx(String txHex, String address, String privateKey) {
        List<SignDto> signDtoList = new ArrayList<>();
        SignDto signDto = new SignDto();
        signDto.setAddress(address);
        signDto.setPriKey(privateKey);
        signDtoList.add(signDto);
        return NulsSDKTool.sign(signDtoList, txHex);
    }

    /**
     * ????????????
     *
     * @param txHex
     * @return
     */
    public Result broadcastTx(String txHex) {
        RpcResult<Map> balanceResult = JsonRpcUtil.request("broadcastTx", ListUtil.of(SDKContext.main_chain_id, txHex));
        RpcResultError rpcResultError = balanceResult.getError();
        if (rpcResultError != null) {
            return Result.getFailed(ErrorCode.init(rpcResultError.getCode())).setMsg(rpcResultError.getMessage());
        }
        Map result = balanceResult.getResult();
        return Result.getSuccess(result);
    }

    /**
     * ????????????
     *
     * @param txHex
     * @return
     */
    public Result validateTx(String txHex) {
        validateChainId();
        try {
            if (StringUtils.isBlank(txHex)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR, "form is empty");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("txHex", txHex);

            RestFulResult restFulResult = RestFulUtil.post("api/accountledger/transaction/validate", map);
            Result result;
            if (restFulResult.isSuccess()) {
                result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
            } else {
                ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
                result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
            }
            return result;
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        }
    }

    public Result createMultiSignConsensusTx(MultiSignConsensusDto consensusDto) {
        validateChainId();
        try {
            if (StringUtils.isBlank(consensusDto.getRewardAddress())) {
                consensusDto.setRewardAddress(consensusDto.getAgentAddress());
            }
            CommonValidator.validateMultiSignConsensusDto(consensusDto);
            if (consensusDto.getInput().getAssetChainId() == 0) {
                consensusDto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (consensusDto.getInput().getAssetId() == 0) {
                consensusDto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.REGISTER_AGENT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            Agent agent = new Agent();
            agent.setAgentAddress(AddressTool.getAddress(consensusDto.getAgentAddress()));
            agent.setPackingAddress(AddressTool.getAddress(consensusDto.getPackingAddress()));
            agent.setRewardAddress(AddressTool.getAddress(consensusDto.getRewardAddress()));
            agent.setDeposit((consensusDto.getDeposit()));
            agent.setCommissionRate((byte) consensusDto.getCommissionRate());
            tx.setTxData(agent.serialize());

            CoinData coinData = assemblyCoinData(consensusDto.getInput(), agent.getDeposit(), consensusDto.getPubKeys().size(), tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) consensusDto.getMinSigns());
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : consensusDto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    private CoinData assemblyCoinData(CoinFromDto from, BigInteger amount, int pubKeyCount, int txSize) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();

        byte[] address = AddressTool.getAddress(from.getAddress());
        byte[] nonce = HexUtil.decode(from.getNonce());
        CoinFrom coinFrom = new CoinFrom(address, from.getAssetChainId(), from.getAssetId(), from.getAmount(), nonce, AccountConstant.NORMAL_TX_LOCKED);
        coinFroms.add(coinFrom);

        List<CoinTo> coinTos = new ArrayList<>();
        CoinTo coinTo = new CoinTo(address, from.getAssetChainId(), from.getAssetId(), amount, -1);
        coinTos.add(coinTo);

        txSize = txSize + getMultiSignSignatureSize(pubKeyCount);
        TxUtils.calcTxFee(coinFroms, coinTos, txSize);
        CoinData coinData = new CoinData();
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        return coinData;
    }

    public Result createMultiSignDepositTx(MultiSignDepositDto dto) {
        validateChainId();
        try {
            CommonValidator.validateMultiSignDepositDto(dto);
            if (dto.getInput().getAssetChainId() == 0) {
                dto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (dto.getInput().getAssetId() == 0) {
                dto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.DEPOSIT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            Deposit deposit = new Deposit();
            deposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            deposit.setAgentHash(NulsHash.fromHex(dto.getAgentHash()));
            deposit.setDeposit(dto.getDeposit());
            tx.setTxData(deposit.serialize());

            CoinData coinData = assemblyCoinData(dto.getInput(), dto.getDeposit(), dto.getPubKeys().size(), tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) dto.getMinSigns());
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : dto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    /**
     * ????????????????????????
     *
     * @param dto ????????????????????????
     * @return result
     */
    public Result createMultiSignWithdrawDepositTx(MultiSignWithDrawDto dto) {
        validateChainId();
        try {
            if (dto.getPrice() == null) {
                dto.setPrice(NULS_DEFAULT_OTHER_TX_FEE_PRICE);
            }
            CommonValidator.validateMultiSignWithDrawDto(dto);
            if (dto.getInput().getAssetChainId() == 0) {
                dto.getInput().setAssetChainId(SDKContext.main_chain_id);
            }
            if (dto.getInput().getAssetId() == 0) {
                dto.getInput().setAssetId(SDKContext.main_asset_id);
            }

            Transaction tx = new Transaction(TxType.CANCEL_DEPOSIT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            cancelDeposit.setJoinTxHash(NulsHash.fromHex(dto.getDepositHash()));
            tx.setTxData(cancelDeposit.serialize());

            CoinData coinData = assemblyCoinData(dto, tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) dto.getMinSigns());
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : dto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);

        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    public Result createMultiSignStopConsensusTx(MultiSignStopConsensusDto dto) {
        validateChainId();
        try {
            if (dto.getPrice() == null) {
                dto.setPrice(NULS_DEFAULT_OTHER_TX_FEE_PRICE);
            }
            CommonValidator.validateMultiSignStopConsensusDto(dto);
            for (StopDepositDto depositDto : dto.getDepositList()) {
                if (depositDto.getInput().getAssetChainId() == 0) {
                    depositDto.getInput().setAssetChainId(SDKContext.main_chain_id);
                }
                if (depositDto.getInput().getAssetId() == 0) {
                    depositDto.getInput().setAssetId(SDKContext.main_asset_id);
                }
            }

            Transaction tx = new Transaction(TxType.STOP_AGENT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

            StopAgent stopAgent = new StopAgent();
            NulsHash nulsHash = NulsHash.fromHex(dto.getAgentHash());
            stopAgent.setCreateTxHash(nulsHash);
            tx.setTxData(stopAgent.serialize());

            CoinData coinData = assemblyCoinData(dto, tx.size());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) dto.getMinSigns());
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : dto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }

    public Result createMultiSignAliasTx(MultiSignAliasDto aliasDto) {
        validateChainId();
        try {
            CommonValidator.validateMultiSignAliasDto(aliasDto);

            Transaction tx = new Transaction(TxType.ACCOUNT_ALIAS);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            tx.setRemark(StringUtils.bytes(aliasDto.getRemark()));

            Alias alias = new Alias(AddressTool.getAddress(aliasDto.getAddress()), aliasDto.getAlias());
            tx.setTxData(alias.serialize());

            CoinData coinData = assemblyCoinData(aliasDto);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            MultiSignTxSignature signature = new MultiSignTxSignature();
            signature.setM((byte) aliasDto.getMinSigns());
            List<byte[]> list = new ArrayList<>();
            for (String pubKey : aliasDto.getPubKeys()) {
                list.add(HexUtil.decode(pubKey));
            }
            signature.setPubKeyList(list);
            tx.setTransactionSignature(signature.serialize());

            Map<String, Object> map = new HashMap<>();
            map.put("hash", tx.getHash().toHex());
            map.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode()).setMsg(e.format());
        } catch (IOException e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR).setMsg(AccountErrorCode.DATA_PARSE_ERROR.getMsg());
        }
    }
}

