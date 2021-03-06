package io.icw.chain.storage.impl;

import io.icw.chain.model.po.CacheDatas;
import io.icw.chain.storage.CacheDatasStorage;
import io.icw.chain.storage.InitDB;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.core.basic.InitializingBean;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.ByteUtils;
import io.icw.core.exception.NulsException;

/**
 * @author lan
 * @date 2019/1/8
 */
@Component
public class CacheDatasStorageImpl extends BaseStorage implements CacheDatasStorage, InitDB, InitializingBean {

    private final String TBL = "module_block_datas_bak";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }


    @Override
    public void save(long key, CacheDatas moduleTxDatas) throws Exception {
        RocksDBService.put(TBL, ByteUtils.longToBytes(key), moduleTxDatas.serialize());
    }


    @Override
    public void delete(long key) throws Exception {
        RocksDBService.delete(TBL, ByteUtils.longToBytes(key));
    }


    @Override
    public CacheDatas load(long key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, ByteUtils.longToBytes(key));
        if (bytes == null) {
            return null;
        }
        CacheDatas moduleTxDatas = new CacheDatas();
        moduleTxDatas.parse(bytes, 0);
        return moduleTxDatas;
    }

    @Override
    public void initTableName() throws NulsException {
       super.initTableName(TBL);
    }
}
