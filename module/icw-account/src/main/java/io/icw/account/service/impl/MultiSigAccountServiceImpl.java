/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.icw.account.service.impl;

import io.icw.account.constant.AccountErrorCode;
import io.icw.account.model.bo.Account;
import io.icw.account.model.bo.Chain;
import io.icw.account.model.po.MultiSigAccountPO;
import io.icw.account.service.AccountService;
import io.icw.account.service.AliasService;
import io.icw.account.service.MultiSignAccountService;
import io.icw.account.service.TransactionService;
import io.icw.account.storage.MultiSigAccountStorageService;
import io.icw.account.util.LoggerUtil;
import io.icw.account.util.manager.ChainManager;
import io.icw.base.basic.AddressTool;
import io.icw.base.data.Address;
import io.icw.base.data.MultiSigAccount;
import io.icw.core.constant.BaseConstant;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.HexUtil;
import io.icw.core.exception.NulsException;
import io.icw.core.exception.NulsRuntimeException;
import io.icw.core.parse.SerializeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: EdwardChan
 * <p>
 * Dec.20th 2018
 */
@Component
public class MultiSigAccountServiceImpl implements MultiSignAccountService {
    @Autowired
    private MultiSigAccountStorageService multiSigAccountStorageService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MultiSignAccountService multiSignAccountService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionService transactionService;

    /**
     * ??????????????????????????????????????????????????????????????????
     * @param chainId
     * @param pubKeys
     * @return ??????????????????????????????????????????
     */
    private List<String> getOriginalPubKeys(int chainId, List<String> pubKeys){
        //for(String pubKey: pubKeys){
        for(int i=0; i < pubKeys.size();i++){
            String pubKey = pubKeys.get(i);
            if(AddressTool.validAddress(chainId, pubKey)) {
                if (AddressTool.isMultiSignAddress(pubKey)) {
                    //???????????????????????????????????????
                    throw new NulsRuntimeException(AccountErrorCode.CONTRACT_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT);
                } else if (AddressTool.validContractAddress(AddressTool.getAddress(pubKey), chainId)) {
                    //?????????????????????????????????????????????
                    throw new NulsRuntimeException(AccountErrorCode.MULTISIG_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT);
                } else if (AddressTool.validNormalAddress(AddressTool.getAddress(pubKey), chainId)) {
                    //????????????
                    Account account = accountService.getAccount(chainId, pubKey);
                    if (account == null) {
                        //?????????????????????
                        throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                    }
                    pubKeys.set(i, HexUtil.encode(account.getPubKey()));
                }
            }
        }
        return pubKeys;
    }

    @Override
    public MultiSigAccount createMultiSigAccount(Chain chain, List<String> pubKeys, int minSigns) throws NulsException {
        MultiSigAccount multiSigAccount = null;
        int chainId = chain.getChainId();
        //??????????????????????????????????????????????????????,??????????????????????????????????????????,????????????????????????????????????????????????
        getOriginalPubKeys(chainId, pubKeys);
        //????????????????????????
        Set<String> pubkeySet = new HashSet<>(pubKeys);
        if(pubkeySet.size() < pubKeys.size()){
           throw new NulsException(AccountErrorCode.PUBKEY_REPEAT);
        }
        Address address = null;
        try {
            address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(AddressTool.createMultiSigAccountOriginBytes(chainId, minSigns, pubKeys)));
        } catch (Exception e) {
            chain.getLogger().error(e);
            throw new NulsException(AccountErrorCode.CREATE_MULTISIG_ADDRESS_FAIL);
        }
        multiSigAccount = this.saveMultiSigAccount(chainId, address, pubKeys, minSigns);
        return multiSigAccount;
    }


    @Override
    public MultiSigAccount getMultiSigAccountByAddress(byte[] address) {
        MultiSigAccount multiSigAccount = null;
        try {
            MultiSigAccountPO multiSigAccountPo = multiSigAccountStorageService.getAccount(address);
            if (multiSigAccountPo != null) {
                multiSigAccount = multiSigAccountPo.toAccount();
            }
        } catch (Exception e) {
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return multiSigAccount;
    }

    @Override
    public MultiSigAccount getMultiSigAccountByAddress(String address) {
        return getMultiSigAccountByAddress(AddressTool.getAddress(address));
    }

    @Override
    public boolean removeMultiSigAccount(int chainId, String address) {
        boolean result;
        try {
            byte[] addressBytes = AddressTool.getAddress(address);
            MultiSigAccountPO multiSigAccountPo = this.multiSigAccountStorageService.getAccount(addressBytes);
            if (multiSigAccountPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.MULTISIGN_ACCOUNT_NOT_EXIST);
            }
            Address addressObj = new Address(address);
            result = multiSigAccountStorageService.removeAccount(addressObj);
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return result;
    }

    private MultiSigAccount saveMultiSigAccount(int chainId, Address addressObj, List<String> pubKeys, int minSigns) {
        MultiSigAccount multiSigAccount = null;
        MultiSigAccountPO multiSigAccountPo = new MultiSigAccountPO();
        multiSigAccountPo.setChainId(chainId);
        multiSigAccountPo.setAddress(addressObj);
        List<byte[]> list = new ArrayList<>();
        for (String pubKey : pubKeys) {
            list.add(HexUtil.decode(pubKey));
        }
        multiSigAccountPo.setPubKeyList(list);
        multiSigAccountPo.setM((byte) minSigns);
        //??????????????????(?????????)
        multiSigAccountPo.setAlias(aliasService.getAliasByAddress(chainId, addressObj.getBase58()));
        boolean result = this.multiSigAccountStorageService.saveAccount(multiSigAccountPo);
        if (result) {
            multiSigAccount = multiSigAccountPo.toAccount();
        }
        return multiSigAccount;
    }

}
