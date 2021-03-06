package io.icw.test.cases.transcation;

import io.icw.test.cases.CaseType;
import io.icw.test.cases.SleepAdapter;
import io.icw.test.cases.TestCaseChain;
import io.icw.test.cases.TestCaseIntf;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-22 11:52
 * @Description: 功能描述
 */
@Component
public class ReadyBalanceToAddressAdapter extends TestCaseChain {

    @Override
    public CaseType caseType(){
        return CaseType.Adapter;
    }

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                TransferToAddressCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationToAddressAdapter.class
        };
    }

    @Override
    public String title() {
        return "给指定地址准备余额";
    }
}
