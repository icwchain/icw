package io.icw.test.cases.transcation;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.transaction.facade.TransferReq;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.Constants;
import io.icw.core.core.annotation.Component;
import static io.icw.test.cases.Constants.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:13
 * @Description: 从种子账户注入10个Nuls到指定账户
 */
@Component
public class TransferToAddressCase extends BaseTranscationCase<String, String> {

    @Override
    public String title() {
        return "转账到指定账户";
    }

    @Override
    public String doTest(String toAddress, int depth) throws TestFailException {
        String formAddress = config.getSeedAddress();
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                        .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                        .addTo(toAddress, TRANSFER_AMOUNT);
        builder.setRemark(REMARK);
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        checkResultStatus(result);
        return result.getData();
    }
}
