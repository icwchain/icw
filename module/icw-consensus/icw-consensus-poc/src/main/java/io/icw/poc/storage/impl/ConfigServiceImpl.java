package io.icw.poc.storage.impl;

import io.icw.core.core.annotation.Component;
import io.icw.core.rockdb.model.Entry;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.poc.model.bo.config.ConfigBean;
import io.icw.poc.constant.ConsensusConstant;
import io.icw.poc.storage.ConfigService;
import io.icw.core.model.ByteUtils;
import io.icw.core.log.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置信息存储管理类
 * Configuration Information Storage Management Class
 *
 * @author tag
 * 2018/11/8
 * */
@Component
public class ConfigServiceImpl implements ConfigService {
    @Override
    public boolean save(ConfigBean bean, int chainID) throws Exception{
        if(bean == null){
            return  false;
        }
        return RocksDBService.put(ConsensusConstant.DB_NAME_CONSUME_CONGIF, ByteUtils.intToBytes(chainID), bean.serialize());
    }

    @Override
    public ConfigBean get(int chainID) {
        try {
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSUME_CONGIF,ByteUtils.intToBytes(chainID));
            ConfigBean configBean = new ConfigBean();
            configBean.parse(value,0);
            return configBean;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainID) {
        try {
            return RocksDBService.delete(ConsensusConstant.DB_NAME_CONSUME_CONGIF,ByteUtils.intToBytes(chainID));
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public Map<Integer, ConfigBean> getList() {
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
            Map<Integer, ConfigBean> configBeanMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            for (Entry<byte[], byte[]>entry:list) {
                int key = ByteUtils.bytesToInt(entry.getKey());
                ConfigBean configBean = new ConfigBean();
                configBean.parse(entry.getValue(),0);
                configBeanMap.put(key,configBean);
            }
            return configBeanMap;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    /*@Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }*/
}
