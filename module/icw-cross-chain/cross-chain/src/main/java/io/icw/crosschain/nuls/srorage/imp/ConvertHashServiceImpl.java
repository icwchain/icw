package io.icw.crosschain.nuls.srorage.imp;

import io.icw.base.data.NulsHash;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.core.rockdb.model.Entry;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.crosschain.nuls.constant.NulsCrossChainConstant;
import io.icw.crosschain.nuls.srorage.ConvertHashService;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨链交易协议Hash对应表处理类
 * Hash Correspondence Table of Cross-Chain Transaction Protocol
 *
 * @author  tag
 * 2019/6/19
 * */
@Component
public class ConvertHashServiceImpl implements ConvertHashService {
    @Override
    public boolean save(NulsHash originalHash, NulsHash localHash, int chainID) {
        if(originalHash == null || localHash == null){
            return false;
        }
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX+chainID,originalHash.getBytes(),localHash.getBytes());
        }catch(Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public NulsHash get(NulsHash originalHash, int chainID) {
        if(originalHash == null){
            return null;
        }
        try {
            byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX+chainID, originalHash.getBytes());
            if(valueBytes == null){
                return null;
            }
            return new NulsHash(valueBytes);
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(NulsHash originalHash, int chainID) {
        try {
            if(originalHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX+chainID,originalHash.getBytes());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<NulsHash> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX+chainID);
            List<NulsHash> hashList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                NulsHash localHash = new NulsHash(entry.getValue());
                hashList.add(localHash);
            }
            return hashList;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
