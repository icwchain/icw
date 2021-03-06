package io.icw.test.cases.transcation;

import io.icw.base.api.provider.transaction.facade.TransactionData;
import io.icw.test.cases.BaseAdapter;
import io.icw.test.cases.CaseType;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:59
 * @Description: 功能描述
 */
@Component
public class GetTranscationFormAddressAdapter extends BaseAdapter<String, TransactionData> {

    @Override
    public String title() {
        return "从交易对象中提取出金地址";
    }

    @Override
    public String doTest(TransactionData param, int depth) throws TestFailException {
            return param.getFrom().get(0).getAddress();
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }
}
