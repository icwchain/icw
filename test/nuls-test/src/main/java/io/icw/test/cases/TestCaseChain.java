package io.icw.test.cases;

import io.icw.core.core.ioc.SpringLiteContext;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:18
 * @Description: 测试用例链
 */
public abstract class TestCaseChain extends BaseTestCase<Object,Object> {

    public abstract Class<? extends TestCaseIntf>[] testChain();

    @Override
    public Object doTest(Object param,int depth) throws TestFailException {
        Class<? extends TestCaseIntf>[] testCases = this.testChain();
        for (Class tc: testCases) {

            TestCaseIntf t = (TestCaseIntf) SpringLiteContext.getBean(tc);
            param = t.check(param,depth);
        }
        return param;
    }

}
