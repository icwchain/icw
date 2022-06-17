/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.icw.chain.storage.impl;

import io.icw.chain.model.po.BlockHeight;
import io.icw.chain.storage.BlockHeightStorage;
import io.icw.chain.storage.InitDB;
import io.icw.chain.util.LoggerUtil;
import io.icw.core.basic.InitializingBean;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;

/**
 * @author lan
 * @description
 * @date 2019/02/20
 **/
@Component
public class BlockHeightStorageImpl  extends  BaseStorage implements BlockHeightStorage, InitDB,InitializingBean {
    private final String TBL = "txs_block_height";


    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }

    @Override
    public BlockHeight getBlockHeight(int chainId) {
        byte[] stream = RocksDBService.get(TBL, ByteUtils.intToBytes(chainId));
        if (stream == null) {
            return null;
        }
        try {
            BlockHeight blockHeight = new BlockHeight();
            blockHeight.parse(stream, 0);
            return blockHeight;
        } catch (Exception e) {
            LoggerUtil.logger().error("getBlockHeight serialize error.", e);
        }
        return null;
    }

    @Override
    public void saveOrUpdateBlockHeight(int chainId, BlockHeight blockHeight) throws Exception {
        LoggerUtil.logger().info("chainId = {},blockHeight={} saveOrUpdateBlockHeight", chainId, blockHeight);
        RocksDBService.put(TBL, ByteUtils.intToBytes(chainId), blockHeight.serialize());
    }

    @Override
    public void initTableName() throws NulsException {
         super.initTableName(TBL);
    }
}
