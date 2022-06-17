package io.icw.contract.tx.v1;

import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.contract.model.bo.ContractResult;
import io.icw.contract.model.bo.ContractWrapperTransaction;
import io.icw.contract.model.dto.ContractPackageDto;
import io.icw.contract.model.tx.CreateContractTransaction;
import io.icw.contract.model.txdata.CreateContractData;
import io.icw.contract.processor.CreateContractTxProcessor;
import io.icw.contract.helper.ContractHelper;
import io.icw.contract.manager.ChainManager;
import io.icw.contract.util.Log;
import io.icw.contract.validator.CreateContractTxValidator;
import io.icw.core.basic.Result;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("CreateContractProcessor")
public class CreateContractProcessor implements TransactionProcessor {

    @Autowired
    private CreateContractTxProcessor createContractTxProcessor;
    @Autowired
    private CreateContractTxValidator createContractTxValidator;
    @Autowired
    private ContractHelper contractHelper;

    @Override
    public int getType() {
        return TxType.CREATE_CONTRACT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        ChainManager.chainHandle(chainId);
        Map<String, Object> result = new HashMap<>();
        List<Transaction> errorList = new ArrayList<>();
        result.put("txList", errorList);
        String errorCode = null;
        CreateContractTransaction createTx;
        for(Transaction tx : txs) {
            createTx = new CreateContractTransaction();
            createTx.copyTx(tx);
            try {
                Result validate = createContractTxValidator.validate(chainId, createTx);
                if(validate.isFailed()) {
                    errorCode = validate.getErrorCode().getCode();
                    errorList.add(tx);
                }
            } catch (NulsException e) {
                Log.error(e);
                errorCode = e.getErrorCode().getCode();
                errorList.add(tx);
            }
        }
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader header) {
        try {
            ContractPackageDto contractPackageDto = contractHelper.getChain(chainId).getBatchInfo().getContractPackageDto();
            if (contractPackageDto != null) {
                Map<String, ContractResult> contractResultMap = contractPackageDto.getContractResultMap();
                ContractResult contractResult;
                ContractWrapperTransaction wrapperTx;
                String txHash;
                for (Transaction tx : txs) {
                    txHash = tx.getHash().toString();
                    contractResult = contractResultMap.get(txHash);
                    if (contractResult == null) {
                        Log.warn("empty contract result with txHash: {}", txHash);
                        continue;
                    }
                    wrapperTx = contractResult.getTx();
                    wrapperTx.setContractResult(contractResult);
                    createContractTxProcessor.onCommit(chainId, wrapperTx);
                }
            }

            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            ChainManager.chainHandle(chainId);
            CreateContractData create;
            for (Transaction tx : txs) {
                create = new CreateContractData();
                create.parse(tx.getTxData(), 0);
                createContractTxProcessor.onRollback(chainId, new ContractWrapperTransaction(tx, create));
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
}
