package io.icw.test.cases.transcation.batch;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.transaction.facade.TransferReq;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.thread.ThreadUtils;
import io.icw.test.cases.Constants;
import io.icw.test.cases.SleepAdapter;
import io.icw.test.cases.TestFailException;
import io.icw.test.cases.transcation.BaseTranscationCase;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static io.icw.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 13:52
 * @Description: 功能描述
 */
@Component
public class BatchCreateTransferCase2 extends BaseTranscationCase<Boolean, BatchParam> {

    int THEADH_COUNT = 1;

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    @Autowired
    BatchCreateAccountCase2 batchCreateAccountCase;

    @Override
    public String title() {
        return "批量创建交易";
    }

    @Autowired
    SleepAdapter.$15SEC sleep15;


    @Override
    public Boolean doTest(BatchParam param, int depth) throws TestFailException {
        Long count = param.count;
        List<String> from;
        List<String> to;
        if(param.getReverse()){
            from = batchCreateAccountCase.getToList();
            to = batchCreateAccountCase.getFormList();
        }else{
            from = batchCreateAccountCase.getFormList();
            to = batchCreateAccountCase.getToList();
        }
        AtomicInteger doneTotal = new AtomicInteger(0);
        AtomicInteger successTotal = new AtomicInteger(0);
        Long start = System.currentTimeMillis();
        Log.info("开始创建交易");
        CountDownLatch latch = new CountDownLatch(THEADH_COUNT);
        for (int s = 0; s < THEADH_COUNT; s++) {
            ThreadUtils.createAndRunThread("batch-transfer", () -> {
                int i = doneTotal.getAndIncrement();
                while (i < count) {
                    int index = i % batchCreateAccountCase.getFormList().size();
                    String formAddress = from.get(index);
                    String toAddress = to.get(index);
                    TransferReq.TransferReqBuilder builder =
                            new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                    .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                    .addTo(toAddress, TRANSFER_AMOUNT);
                    builder.setRemark(REMARK);
                    Result<String> result = transferService.transfer(builder.build(new TransferReq()));
                    try {
                        checkResultStatus(result);
                        successTotal.getAndIncrement();
                    } catch (TestFailException e) {
                        Log.error("创建交易失败:{}", e.getMessage());
                    }
                    i = doneTotal.getAndIncrement();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", count, successTotal, System.currentTimeMillis() - start);
        return true;
    }

}
