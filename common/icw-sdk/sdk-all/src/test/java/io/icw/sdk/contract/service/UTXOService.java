package io.icw.sdk.contract.service;

import io.icw.sdk.accountledger.model.Input;

import java.util.List;

/**
 * Created by wangkun23 on 2018/10/9.
 */
public interface UTXOService {
    /**
     * get utxos by address and amount
     *
     * @param address
     * @param amount
     * @return
     */
    List<Input> getUTXOs(String address, Long amount);
}
