package io.icw.transaction.constant;

import io.icw.core.basic.ModuleConfig;
import io.icw.core.basic.VersionChangeInvoker;
import io.icw.core.core.annotation.Component;
import io.icw.core.core.annotation.Configuration;
import io.icw.core.rpc.model.ModuleE;
import io.icw.transaction.model.bo.config.ConfigBean;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Transaction module setting
 * @author: Charlie
 * @date: 2019/03/14
 */
@Component
@Configuration(domain = ModuleE.Constant.TRANSACTION)
public class TxConfig extends ConfigBean implements ModuleConfig {
    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;
    /** 模块code*/
    private String moduleCode;
    /** 主链链ID*/
    private int mainChainId;
    /** 主链主资产ID*/
    private int mainAssetId;
    /** 编码*/
    private String encoding;
    /** 未确认交易过期时间秒 */
    private long unconfirmedTxExpire;
    private String blackHolePublicKey;

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }
    // add by pierre at 2019-12-04
    /**
     *  是否已连接智能合约模块
     */
    private volatile boolean collectedSmartContractModule;

    public boolean isCollectedSmartContractModule() {
        return collectedSmartContractModule;
    }

    public void setCollectedSmartContractModule(boolean collectedSmartContractModule) {
        this.collectedSmartContractModule = collectedSmartContractModule;
    }
    // end code by pierre

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getTxDataRoot() {
        return dataPath + File.separator + ModuleE.TX.name;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public int getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(int mainChainId) {
        this.mainChainId = mainChainId;
    }

    public int getMainAssetId() {
        return mainAssetId;
    }

    public void setMainAssetId(int mainAssetId) {
        this.mainAssetId = mainAssetId;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public long getUnconfirmedTxExpire() {
        return unconfirmedTxExpire;
    }

    public void setUnconfirmedTxExpire(long unconfirmedTxExpire) {
        this.unconfirmedTxExpire = unconfirmedTxExpire;
    }

    @Override
    public VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> aClass = Class.forName("io.icw.transaction.rpc.upgrade.TxVersionChangeInvoker");
        return (VersionChangeInvoker) aClass.getDeclaredConstructor().newInstance();
    }
}
