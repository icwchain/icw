package io.icw.test.cases.account;

import io.icw.base.api.provider.account.facade.GetAccountByAddressReq;
import io.icw.base.api.provider.account.facade.RemoveAccountReq;
import io.icw.test.cases.TestFailException;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 18:02
 * @Description: 功能描述
 */
@Component
public class RemoveAccountCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "删除账户";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        checkResultStatus(accountService.removeAccount(new RemoveAccountReq(PASSWORD,param)));
        check(accountService.getAccountByAddress(new GetAccountByAddressReq(param)).getData() == null,"删除账户失败");
        return param;
    }
}
