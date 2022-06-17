package io.icw.chain.storage;


import io.icw.chain.model.po.Asset;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
public interface AssetStorage {

    /**
     * Save asset
     *
     * @param key   ChainId_AssetId
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    void save(String key, Asset asset) throws Exception;

    /**
     * @param kvs
     * @throws Exception
     */
    void batchSave(Map<byte[], byte[]> kvs) throws Exception;

    /**
     * Find assets based on key
     *
     * @param key ChainId_AssetId
     * @return Asset object
     */
    Asset load(String key) throws Exception;

    /**
     * Physical deletion
     *
     * @param key ChainId_AssetId
     * @return true/false
     */
    void delete(String key) throws Exception;


}
