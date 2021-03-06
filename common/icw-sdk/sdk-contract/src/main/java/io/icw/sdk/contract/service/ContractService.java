package io.icw.sdk.contract.service;

import io.icw.sdk.accountledger.model.Input;
import io.icw.sdk.core.model.Result;

import java.util.List;

public interface ContractService {
    /**
     * create contract transaction
     *
     * @param sender
     * @param gasLimit
     * @param price
     * @param contractCodeHex
     * @param args
     * @param remark
     * @param utxos user account unspent transaction output
     * @return
     */
    Result createContractTransaction(String sender, Long gasLimit, Long price, String contractCodeHex, Object[] args, String remark, List<Input> utxos);

    /**
     * call contract's method
     *
     * @param sender
     * @param value
     * @param gasLimit
     * @param price
     * @param contractAddress
     * @param methodName
     * @param methodDesc
     * @param args
     * @param remark
     * @param utxos
     * @return
     */
    Result callContractTransaction(String sender, Long value, Long gasLimit, Long price, String contractAddress, String methodName, String methodDesc, Object[] args, String remark, List<Input> utxos);

    /**
     * delete smart contract
     *
     * @param sender
     * @param contractAddress
     * @param remark
     * @param utxos
     * @return
     */
    Result deleteContractTransaction(String sender, String contractAddress, String remark, List<Input> utxos);
}
