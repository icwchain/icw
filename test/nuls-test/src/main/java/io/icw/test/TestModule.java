package io.icw.test;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.icw.base.api.provider.network.NetworkProvider;
import io.icw.base.api.provider.network.facade.NetworkInfo;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.I18nUtils;
import io.icw.core.rpc.model.ModuleE;
import io.icw.core.rpc.modulebootstrap.Module;
import io.icw.core.rpc.modulebootstrap.RpcModule;
import io.icw.core.rpc.modulebootstrap.RpcModuleState;
import io.icw.test.cases.Constants;
import io.icw.test.cases.TestCase;
import io.icw.test.cases.TestCaseIntf;
import io.icw.test.cases.TestFailException;
import io.icw.test.controller.RpcServerManager;
import io.icw.test.utils.LoggerUtil;
import io.icw.test.utils.RestFulUtils;
import io.icw.test.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-18 15:05
 * @Description: 功能描述
 */
@Component
public class TestModule extends RpcModule {

    static NulsLogger log = LoggerUtil.logger;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired Config config;


    NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    @Override
    protected long getTryRuningTimeout() {
        return Long.MAX_VALUE;
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                new Module(ModuleE.BL.abbr,"1.0"),
                new Module(ModuleE.TX.abbr,"1.0"),
                new Module(ModuleE.LG.abbr,"1.0"),
                new Module(ModuleE.CS.abbr,"1.0"),
                Module.build(ModuleE.AC)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module("test","1.0");
    }

    @Override
    public boolean doStart()
    {
        log.info("等待依赖模块准备就绪");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("do running");
        RpcServerManager.getInstance().startServer("0.0.0.0", config.getHttpPort());
        if(config.getNodeType().equals("master")){
            Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(Constants.PASSWORD,config.getTestSeedAccount(),true));
            config.setSeedAddress(result.getData());
            Result<NetworkInfo> networkInfo = networkProvider.getInfo();
            Utils.success("=".repeat(100));
            Utils.success("网络环境");
            Utils.success("localBestHeight:"+networkInfo.getData().getLocalBestHeight());
            Utils.success("netBestHeight:"+networkInfo.getData().getNetBestHeight());
            Utils.success("timeOffset:"+networkInfo.getData().getTimeOffset());
            Utils.success("inCount:"+networkInfo.getData().getInCount());
            Utils.success("outCount:"+networkInfo.getData().getOutCount());
            Utils.success("nodes:" + networkProvider.getNodes().getList().toString());
            Utils.success("Time:" + NulsDateUtils.timeStamp2DateStr(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"));
            Utils.success("packetMagic:"+config.getPacketMagic());
            Utils.success("=".repeat(100));
            if(networkProvider.getNodes().getList().size()<config.getTestNodeCount()){
                log.error("网络节点数量小于要求测试的节点数，不能进行测试，要求测试的节点数:{}",config.getTestNodeCount());
                System.exit(0);
            }
            System.out.println();
            System.out.println();
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            try {
                List<TestCaseIntf> testList = SpringLiteContext.getBeanList(TestCaseIntf.class);
                testList.forEach(tester->{
                    TestCase testCase = tester.getClass().getAnnotation(TestCase.class);
                    if(testCase == null){
                        return ;
                    }
                    if(StringUtils.isNotBlank(System.getProperty("test.case"))){
                        String testCaseName = System.getProperty("test.case");
                        if(!testCase.value().equals(testCaseName)){
                            return ;
                        }
                    }
                    try {
                        Utils.successDoubleLine("开始测试"+tester.title() + "   " + tester.getClass());
                        tester.check(null,0);
                    } catch (TestFailException e) {
                        Utils.failLine( "【" + tester.title() + "】 测试失败 :" + e.getMessage());
                        isSuccess.set(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(isSuccess.get()){
                Utils.successLine(" TEST DONE ");
            }else{
                Utils.failLine(" TEST FAIL ");
            }
            System.exit(0);
        }
        return RpcModuleState.Running;
    }

    @Override
    public void onDependenciesReady(Module module) {
        if(module.getName().equals(ModuleE.AC.name)){
            Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(Constants.PASSWORD,config.getTestSeedAccount(),true));
            config.setSeedAddress(result.getData());
        }
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    public void init() {
        super.init();
        I18nUtils.loadLanguage(this.getClass(), "languages", "en");
//        I18nUtils.setLanguage("en");
        RestFulUtils.getInstance().setServerUri("http://127.0.0.1:" + config.getHttpPort() + "/api/");
    }
}
