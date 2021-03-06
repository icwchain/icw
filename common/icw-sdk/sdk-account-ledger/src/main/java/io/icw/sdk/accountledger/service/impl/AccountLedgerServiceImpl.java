package io.icw.sdk.accountledger.service.impl;

import io.icw.sdk.accountledger.model.*;
import io.icw.sdk.accountledger.utils.ConvertCoinTool;
import io.icw.sdk.core.contast.AccountErrorCode;
import io.icw.sdk.core.contast.KernelErrorCode;
import io.icw.sdk.core.contast.SDKConstant;
import io.icw.sdk.core.contast.TransactionErrorCode;
import io.icw.sdk.core.model.*;
import io.icw.sdk.core.script.*;
import io.icw.sdk.core.utils.*;
import io.icw.sdk.accountledger.service.AccountLedgerService;
import io.icw.sdk.accountledger.utils.LedgerUtil;
import io.icw.sdk.core.crypto.AESEncrypt;
import io.icw.sdk.core.crypto.ECKey;
import io.icw.sdk.core.crypto.Hex;
import io.icw.sdk.core.exception.NulsException;
import io.icw.sdk.core.model.dto.BalanceInfo;
import io.icw.sdk.core.model.transaction.TransferTransaction;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Charlie
 */
public class AccountLedgerServiceImpl implements AccountLedgerService {

    private static final AccountLedgerService INSTANCE = new AccountLedgerServiceImpl();

    private AccountLedgerServiceImpl() {

    }

    public static AccountLedgerService getInstance() {
        return INSTANCE;
    }

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public Result getTxByHash(String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }

        Result result = restFul.get("/accountledger/tx/" + hash, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        //重新组装input
        List<Map<String, Object>> inputMaps = (List<Map<String, Object>>) map.get("inputs");
        List<Input> inputs = new ArrayList<>();
        for (Map<String, Object> inputMap : inputMaps) {
            Input inputDto = new Input(inputMap);
            inputs.add(inputDto);
        }
        map.put("inputs", inputs);

        //重新组装output
        List<Map<String, Object>> outputMaps = (List<Map<String, Object>>) map.get("outputs");
        List<Output> outputs = new ArrayList<>();
        for (Map<String, Object> outputMap : outputMaps) {
            Output outputDto = new Output(outputMap);
            outputs.add(outputDto);
        }
        map.put("outputs", outputs);
        Transaction transactionDto = new Transaction(map);
        result.setData(transactionDto);
        return result;
    }

    @Override
    public Result getTxWithBytesByHash(String hash) {
        if (StringUtils.isBlank(hash)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("hash", hash);
        Result result = restFul.get("/tx/bytes/", parameters);
        if (result.isFailed()) {
            return result;
        }
        Map<String, String> resultMap = (Map<String, String>) result.getData();

        byte[] datas = Base64.getDecoder().decode(resultMap.get("value"));
        long height = Long.parseLong(resultMap.get("height"));
        try {
            io.icw.sdk.core.model.transaction.Transaction tx = TransactionTool.getInstance(new NulsByteBuffer(datas));
            tx.setHash(NulsDigestData.fromDigestHex(hash));
            tx.setBlockHeight(height);
            result.setData(tx);
        } catch (Exception e) {
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
        return result;
    }

    @Override
    public Result transfer(String address, String toAddress, String password, long amount, String remark) {
        if (!AddressTool.validAddress(address) || !AddressTool.validAddress(toAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (!validTxRemark(remark)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("toAddress", toAddress);
        parameters.put("password", password);
        parameters.put("amount", amount);
        parameters.put("remark", remark);
        Result result = restFul.post("/accountledger/transfer", parameters);
        return result;
    }

    @Override
    public Result transfer(String address, String toAddress, long amount, String remark) {
        return transfer(address, toAddress, null, amount, remark);
    }

    @Override
    public Result sendToAddress(String address, String toAddress, String password, long amount, String remark) {
        if (!AddressTool.validAddress(address) || !AddressTool.validAddress(toAddress)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        if (!validTxRemark(remark)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", address);
        parameters.put("toAddress", toAddress);
        parameters.put("password", password);
        parameters.put("amount", amount);
        parameters.put("remark", remark);
        Result result = restFul.post("/accountledger/sendToAddress", parameters);
        return result;
    }

    @Override
    public Result sendToAddress(String address, String toAddress, long amount, String remark) {
        return sendToAddress(address, toAddress, null, amount, remark);
    }

    @Override
    public Result multipleAddressTransfer(List<TransferFrom> froms, List<TransferTo> tos, String remark) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("inputs", froms);
        parameters.put("outputs", tos);
        parameters.put("remark", remark);
        Result result = restFul.post("/accountledger/multipleAddressTransfer", parameters);
        return result;
    }

    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    @Override
    public Result getBalance(String address) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Result result = restFul.get("/accountledger/balance/" + address, null);
        if (result.isFailed()) {
            return result;
        }
        Map<String, Object> map = (Map) result.getData();
        map.put("balance", ((Map) map.get("balance")).get("value"));
        map.put("usable", ((Map) map.get("usable")).get("value"));
        map.put("locked", ((Map) map.get("locked")).get("value"));
        BalanceInfo balanceDto = new BalanceInfo(map);
        return result.setData(balanceDto);
    }

    @Override
    public JsonRPCResult getUTXO(String address, long amount) {
        if (!AddressTool.validAddress(address)) {
            return JsonRPCResult.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("jsonrpc", "2.0");
        paramMap.put("method", "getUTXO");
        Object[] objects = {address, amount};
        paramMap.put("params", objects);
        paramMap.put("id", 1);
        try {
            String json = JSONUtils.obj2json(paramMap);
            String result = HttpClientUtil.fetchStringByPost(SDKConstant.NULSCAN_URL, json);
            if (result == null) {
                return JsonRPCResult.getFailed(KernelErrorCode.CONNECTION_ERROR);
            }
            JsonRPCResult rpcResult = JSONUtils.json2pojo(result, JsonRPCResult.class);
            if (rpcResult.getError() != null) {
                return rpcResult;
            }
            List<Input> inputs = new ArrayList<>();
            List<Map> mapList = (List<Map>) rpcResult.getResult();
            if (mapList != null) {
                for (Map map : mapList) {
                    Input input = new Input();
                    input.setFromHash((String) map.get("fromHash"));
                    input.setFromIndex((Integer) map.get("fromIndex"));
                    input.setAddress(address);
                    input.setLockTime(Long.parseLong(map.get("lockTime").toString()));
                    input.setValue(Long.parseLong(map.get("value").toString()));
                    inputs.add(input);
                }
            }
            rpcResult.setResult(inputs);
            return rpcResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result broadcastTransaction(String txHex) {
        Map<String, Object> map = new HashMap<>();
        map.put("txHex", txHex);
        Result result = restFul.post("/accountledger/transaction/broadcast", map);
        return result;
    }

    @Override
    public Result validateTransaction(String txHex) {
        Map<String, Object> map = new HashMap<>();
        map.put("txHex", txHex);
        Result result = restFul.post("/accountledger/transaction/valiTransaction", map);
        return result;
    }

    @Override
    public Result createTransaction(List<Input> inputs, List<Output> outputs, String remark) {
        return createMultipleInputAddressTransaction(inputs, 1, outputs, remark);
    }

    @Override
    public Result createTransaction(String address, String toAddress, long amount, String remark, List<Input> utxos) {
        try {
            if (!AddressTool.validAddress(address) || !AddressTool.validAddress(toAddress)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }
            if (!validTxRemark(remark)) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }

            byte[] remarkBytes = new byte[0];
            try {
                remarkBytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
            }

            byte[] fromBytes = AddressTool.getAddress(address);
            byte[] toBytes = AddressTool.getAddress(toAddress);
            Na values = Na.valueOf(amount);

            TransferTransaction tx = new TransferTransaction();
            tx.setRemark(remarkBytes);
            tx.setTime(TimeService.currentTimeMillis());
            CoinData coinData = new CoinData();
            //如果为多签地址则以脚本方式存储
            Coin toCoin;
            if (toBytes[2] == SDKConstant.P2SH_ADDRESS_TYPE) {
                Script scriptPubkey = SignatureUtil.createOutputScript(toBytes);
                toCoin = new Coin(scriptPubkey.getProgram(), values);
            } else {
                toCoin = new Coin(toBytes, values);
            }
            coinData.getTo().add(toCoin);

            List<Coin> coinList = ConvertCoinTool.convertCoinList(utxos);
            CoinDataResult coinDataResult = TransactionTool.getCoinData(fromBytes, values, tx.size() + coinData.size(), TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES, coinList);

            if (!coinDataResult.isEnough()) {
                return Result.getFailed(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
            coinData.setFrom(coinDataResult.getCoinList());
            // 找零的UTXO
            if (coinDataResult.getChange() != null) {
                coinData.getTo().add(coinDataResult.getChange());
            }
            tx.setCoinData(coinData);

            // 重置为0，重新计算交易对象的size
            tx.setSize(0);
            if (tx.getSize() > TransactionFeeCalculator.MAX_TX_SIZE) {
                return Result.getFailed(TransactionErrorCode.DATA_SIZE_ERROR);
            }

            TransactionCreatedReturnInfo returnInfo = LedgerUtil.makeReturnInfo(tx);
            Map<String, TransactionCreatedReturnInfo> map = new HashMap<>();
            map.put("value", returnInfo);
            return Result.getSuccess().setData(map);

        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    @Override
    public Result createMultipleInputAddressTransaction(List<Input> inputs, int nInputAccount, List<Output> outputs, String remark) {
        if (inputs == null || inputs.isEmpty()) {
            return Result.getFailed("inputs error");
        }
        if (outputs == null || outputs.isEmpty()) {
            return Result.getFailed("outputs error");
        }
        if (nInputAccount <= 0) {
            return Result.getFailed("nInputAccount error");
        }

        byte[] remarkBytes = null;
        if (!StringUtils.isBlank(remark)) {
            try {
                remarkBytes = remark.getBytes(SDKConstant.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return Result.getFailed("remark error");
            }
        }

        List<Coin> outputList = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            Output outputDto = outputs.get(i);
            Coin to = new Coin();
            try {
                if (!AddressTool.validAddress(outputDto.getAddress())) {
                    return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
                }
                byte[] owner = AddressTool.getAddress(outputDto.getAddress());
                if (owner[2] == 3) {
                    Script scriptPubkey = SignatureUtil.createOutputScript(to.getAddress());
                    to.setOwner(scriptPubkey.getProgram());
                } else {
                    to.setOwner(AddressTool.getAddress(outputDto.getAddress()));

                }
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }

            try {
                to.setNa(Na.valueOf(outputDto.getValue()));
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
            }
            if (outputDto.getLockTime() < 0) {
                return Result.getFailed("lockTime error");
            }

            to.setLockTime(outputDto.getLockTime());
            outputList.add(to);
        }

        List<Coin> inputsList = new ArrayList<>();
        String address = null;

        for (int i = 0; i < inputs.size(); i++) {
            Input inputDto = inputs.get(i);
            if (inputDto.getAddress() == null) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }

            byte[] key = Arrays.concatenate(Hex.decode(inputDto.getFromHash()), new VarInt(inputDto.getFromIndex()).encode());
            Coin coin = new Coin();
            coin.setOwner(key);
            coin.setNa(Na.valueOf(inputDto.getValue()));
            coin.setLockTime(inputDto.getLockTime());
            inputsList.add(coin);
        }

        io.icw.sdk.core.model.transaction.Transaction tx = TransactionTool.createTransferTx(inputsList, outputList, remarkBytes);
        // 兜底，防止手续费不足
        // 暂不支持 output 包含脚本交易
        if (!TransactionTool.isFeeEnough(tx, P2PHKSignature.SERIALIZE_LENGTH * nInputAccount, 1)) {
            return Result.getFailed(TransactionErrorCode.FEE_NOT_RIGHT);
        }

        try {
            String txHex = Hex.encode(tx.serialize());
            Map<String, String> map = new HashMap<>();
            map.put("value", txHex);
            return Result.getSuccess().setData(map);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    @Override
    public Result signTransaction(String txHex, String priKey, String address, String password) {
        if (StringUtils.isBlank(priKey)) {
            return Result.getFailed("priKey error");
        }
        if (StringUtils.isBlank(txHex)) {
            return Result.getFailed("txHex error");
        }
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed("address error");
        }

        if (StringUtils.isNotBlank(password)) {
            if (StringUtils.validPassword(password)) {
                //decrypt
                byte[] privateKeyBytes = null;
                try {
                    privateKeyBytes = AESEncrypt.decrypt(Hex.decode(priKey), password);
                } catch (Exception e) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                priKey = Hex.encode(privateKeyBytes);
            } else {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        if (!ECKey.isValidPrivteHex(priKey)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "priKey error");
        }

        ECKey key = ECKey.fromPrivate(new BigInteger(Hex.decode(priKey)));
        try {
            String newAddress = AccountTool.newAddress(key).getBase58();
            if (!newAddress.equals(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }
        } catch (NulsException e) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }

        try {
            byte[] data = Hex.decode(txHex);
            io.icw.sdk.core.model.transaction.Transaction tx = TransactionTool.getInstance(new NulsByteBuffer(data));
            tx = TransactionTool.signTransaction(tx, key);

            txHex = Hex.encode(tx.serialize());
            Map<String, String> map = new HashMap<>();
            map.put("value", txHex);
            return Result.getSuccess().setData(map);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public Result signHash(String hash, String priKey) {
        try {
            ECKey key = ECKey.fromPrivate(new BigInteger(Hex.decode(priKey)));
            byte[] bytes = TransactionTool.signHash(hash, key);
            String sign = Hex.encode(bytes);
            Map map = new HashMap();
            map.put("value", sign);
            return Result.getSuccess().setData(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Result signMultipleAddressTransaction(String txHex, List<String> privKeys, List<String> passwords) {
        if (StringUtils.isBlank(txHex)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "txHex error");
        }
        if (privKeys == null || privKeys.size() == 0) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "privKeys error");
        }

        if (passwords == null || passwords.size() != privKeys.size()) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }

        // decode private key
        List<String> signKeys = new ArrayList<>(privKeys.size());
        for (int i = 0; i < privKeys.size(); i++) {
            String encryptPrivKey = privKeys.get(i);
            String decryptPass = passwords.get(i);
            if (decryptPass == null || decryptPass.trim().length() == 0) {
                // No need decrypt
                signKeys.add(encryptPrivKey);
                continue;
            }

            byte[] privateKeyBytes = null;
            try {
                privateKeyBytes = AESEncrypt.decrypt(Hex.decode(encryptPrivKey), decryptPass);
            } catch (Exception e) {
                return Result.getFailed(AccountErrorCode.DECRYPT_ACCOUNT_ERROR);
            }
            signKeys.add(Hex.encode(privateKeyBytes));
        }

        // conversion private key string to ECKey
        List<ECKey> keys = signKeys.stream()
                .map(p -> ECKey.fromPrivate(new BigInteger(Hex.decode(p))))
                .collect(Collectors.toList());

        // sign the transaction
        try {
            byte[] data = Hex.decode(txHex);
            io.icw.sdk.core.model.transaction.Transaction tx = TransactionTool.getInstance(new NulsByteBuffer(data));
            List<P2PHKSignature> p2PHKSignatures = SignatureUtil.createSignaturesByEckey(tx, keys);
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            txHex = Hex.encode(tx.serialize());
            Map<String, String> map = new HashMap<>();
            map.put("value", txHex);
            return Result.getSuccess().setData(map);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public Result createMSAccountTransferTransaction(MSAccount account, List<Input> inputs, List<Output> outputs, String remark) {
        // Check parameters
        if (account == null || account.getPubKeys() == null || account.getPubKeys().size() == 0) {
            return Result.getFailed("Multiple Signature Account parameters error");
        }
        if (inputs == null || inputs.isEmpty()) {
            return Result.getFailed("inputs error");
        }
        if (outputs == null || outputs.isEmpty()) {
            return Result.getFailed("outputs error");
        }

        inputs.sort(InputCompare.getInstance());

        CoinData coinData = new CoinData();
        for (Input p : inputs) {
            byte[] fromAddr = AddressTool.getAddress(p.getAddress());
            if (fromAddr[2] != SDKConstant.P2SH_ADDRESS_TYPE) {
                return Result.getFailed("input address is not an multiple signature address");
            }
            Script scriptPubkey = SignatureUtil.createOutputScript(fromAddr);
            Coin input = new Coin(scriptPubkey.getProgram(), Na.valueOf(p.getValue()), 0);
            coinData.getFrom().add(input);
        }

        outputs.forEach(p -> {
            byte[] receiverAddr = AddressTool.getAddress(p.getAddress());
            Coin output = null;
            if (receiverAddr[2] == SDKConstant.P2SH_ADDRESS_TYPE) {
                Script scriptPubkey = SignatureUtil.createOutputScript(receiverAddr);
                output = new Coin(scriptPubkey.getProgram(), Na.valueOf(p.getValue()));
            } else {
                output = new Coin(receiverAddr, Na.valueOf(p.getValue()));
            }
            coinData.getTo().add(output);
        });

        TransferTransaction tx = new TransferTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        tx.setCoinData(coinData);
        if (remark != null && remark.trim().length() > 0) {
            tx.setRemark(remark.trim().getBytes());
        }
        TransactionSignature signature = new TransactionSignature();
        Script redeemScript = ScriptBuilder.createNulsRedeemScript(account.getThreshold(), account.getPubKeys());
        signature.setScripts(Collections.singletonList(redeemScript));
        Map<String, Object> map = new HashMap<>();
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            tx.setTransactionSignature(signature.serialize());
            map.put("txdata", Hex.encode(tx.serialize()));
        } catch (IOException e) {
            return Result.getFailed("outputs error");
        }

        return Result.getSuccess().setData(map);
    }

    @Override
    public Result createChangeCoinTransaction(List<Input> inputs, String address) {
        try {
            if (inputs == null || inputs.isEmpty()) {
                return Result.getFailed("inputs error");
            }
            if (StringUtils.isBlank(address) || !AddressTool.validAddress(address)) {
                return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
            }
            Collections.sort(inputs, InputCompare.getInstance());
            int targetSize = 0;
            int size = 0;
            List<String> transactionList = new ArrayList<>();
            Na amount = Na.ZERO;
            boolean newTransaction = true;
            TransferTransaction tx = null;
            CoinData coinData = null;
            List<String> ownerHexList = null;
            int signType = 1;
            for (int i = 0; i < inputs.size(); i++) {
                Input input = inputs.get(i);
                if (input.getAddress() == null || !input.getAddress().equals(address)) {
                    return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
                }
                //判断是否需创建新交易
                if (newTransaction) {
                    tx = new TransferTransaction();
                    tx.setTime(TimeService.currentTimeMillis());
                    size = tx.getSize() + 38;
                    coinData = new CoinData();
                    targetSize = TransactionTool.MAX_TX_SIZE - size - P2PHKSignature.SERIALIZE_LENGTH;
                    amount = Na.ZERO;
                    signType = 1;
                    ownerHexList = new ArrayList<>();
                    newTransaction = false;
                }
                byte[] key = Arrays.concatenate(Hex.decode(input.getFromHash()), new VarInt(input.getFromIndex()).encode());
                Coin coin = new Coin();
                coin.setOwner(key);
                coin.setNa(Na.valueOf(input.getValue()));
                coin.setLockTime(input.getLockTime());
                size += coin.size();
                ownerHexList.add(Hex.encode(key));
                //判断当前交易中的UTXO是否存在脚本签名的交易
                /*if (size > targetSize - P2PHKSignature.SERIALIZE_LENGTH || i == inputs.size()-1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("utxoList", ownerHexList);
                    Result result = restFul.post("/accountledger/multiAccount/getSignType", map);
                    Map<String, Object> resultMap = (Map) result.getData();
                    signType = Integer.parseInt((String) resultMap.get("signType"));
                    //如果两种签名都存在
                    if ((signType & 0x01) == 0x01 && (signType & 0x02) == 0x02) {
                       size += P2PHKSignature.SERIALIZE_LENGTH;
                    }
                }*/
                if (i == 127) {
                    size += 1;
                }
                if (size > targetSize || i == inputs.size() - 1) {
                    if (i == inputs.size() - 1 && size <= targetSize) {
                        coinData.getFrom().add(coin);
                        tx.setCoinData(coinData);
                        amount = amount.add(coin.getNa());
                    }
                    Na fee = TransactionFeeCalculator.getFee(size, TransactionFeeCalculator.MIN_PRECE_PRE_1024_BYTES);
                    amount = amount.subtract(fee);
                    Coin toCoin = new Coin(AddressTool.getAddress(address), amount);
                    coinData.getTo().add(toCoin);
                    tx.setCoinData(coinData);
                    tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
                    transactionList.add(Hex.encode(tx.serialize()));
                    if (size > targetSize) {
                        i--;
                        newTransaction = true;
                    }
                    continue;
                }
                coinData.getFrom().add(coin);
                tx.setCoinData(coinData);
                amount = amount.add(coin.getNa());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("value", transactionList);
            return Result.getSuccess().setData(map);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public Result signMSTransaction(String txHex, List<String> privKeys, List<String> passwords) {
        try {
            if (StringUtils.isBlank(txHex)) {
                return Result.getFailed("txHex can not be null!");
            }
            if (privKeys == null || privKeys.size() == 0) {
                return Result.getFailed("The privKeys list can not be null!");
            }
            if (passwords == null || passwords.size() == 0) {
                return Result.getFailed("The passwords list can not be null!");
            }
            if (passwords.size() != privKeys.size()) {
                return Result.getFailed("privKeys length and passwords length are not equal,If there is no password in the account, please empty the string.");
            }
            Set<String> priKeySet = new HashSet<>(privKeys);
            if (priKeySet.size() != privKeys.size()) {
                return Result.getFailed("Private key can not be repeated!");
            }
            io.icw.sdk.core.model.transaction.Transaction tx = TransactionTool.getInstance(new NulsByteBuffer(Hex.decode(txHex)));
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            if (transactionSignature == null || transactionSignature.getScripts().size() != 1) {
                return Result.getFailed("Transaction data error!");
            }
            int n = SignatureUtil.getM(transactionSignature.getScripts().get(0));
            if (n != privKeys.size()) {
                return Result.getFailed("The number of private keys is larger than the number of accounts that need to be signed!");
            }
            for (int i = 0; i < privKeys.size(); i++) {
                String priKey = privKeys.get(i);
                String password = passwords.get(i);
                priKey = getPrikey(priKey, password);
                if (StringUtils.isBlank(priKey)) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
                if (!ECKey.isValidPrivteHex(priKey)) {
                    return Result.getFailed(AccountErrorCode.PARAMETER_ERROR, "priKey error");
                }
                ECKey key = ECKey.fromPrivate(new BigInteger(Hex.decode(priKey)));
                transactionSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
                SignatureUtil.createMultiTransactionSignture(tx, transactionSignature, key);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("value", Hex.encode(tx.serialize()));
            return Result.getSuccess().setData(map);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed("Transaction signature error!");
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.DATA_PARSE_ERROR);
        }
    }

    public String getPrikey(String prikey, String password) {
        if (StringUtils.isNotBlank(password)) {
            if (StringUtils.validPassword(password)) {
                //decrypt
                byte[] privateKeyBytes = null;
                try {
                    privateKeyBytes = AESEncrypt.decrypt(Hex.decode(prikey), password);
                } catch (Exception e) {
                    return "";
                }
                prikey = Hex.encode(privateKeyBytes);
            } else {
                return "";
            }
        }
        return prikey;
    }
}
