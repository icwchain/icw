/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.icw.protocol.storage.impl;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.core.core.annotation.Component;
import io.icw.core.model.ByteUtils;
import io.icw.core.rockdb.service.RocksDBService;
import io.icw.protocol.constant.Constant;
import io.icw.protocol.manager.ContextManager;
import io.icw.protocol.model.ChainParameters;
import io.icw.protocol.storage.ParametersStorageService;

import java.util.ArrayList;
import java.util.List;

import static io.icw.protocol.utils.LoggerUtil.COMMON_LOG;

/**
 * 参数持久化类实现
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/23 11:02
 */
@Component
public class ParametersStorageServiceImpl implements ParametersStorageService {
    @Override
    public boolean save(ChainParameters parameters, int chainId) {
        byte[] bytes;
        try {
            bytes = parameters.serialize();
            return RocksDBService.put(Constant.PROTOCOL_CONFIG, ByteUtils.intToBytes(chainId), bytes);
        } catch (Exception e) {
            ContextManager.getContext(chainId).getLogger().error(e);
            return false;
        }
    }

    @Override
    public ChainParameters get(int chainId) {
        try {
            ChainParameters parameters = new ChainParameters();
            byte[] bytes = RocksDBService.get(Constant.PROTOCOL_CONFIG, ByteUtils.intToBytes(chainId));
            parameters.parse(new NulsByteBuffer(bytes));
            return parameters;
        } catch (Exception e) {
            ContextManager.getContext(chainId).getLogger().error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainId) {
        try {
            return RocksDBService.delete(Constant.PROTOCOL_CONFIG, ByteUtils.intToBytes(chainId));
        } catch (Exception e) {
            ContextManager.getContext(chainId).getLogger().error(e);
            return false;
        }
    }

    @Override
    public List<ChainParameters> getList() {
        try {
            var pos = new ArrayList<ChainParameters>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.PROTOCOL_CONFIG);
            for (byte[] bytes : valueList) {
                var parameters = new ChainParameters();
                parameters.parse(new NulsByteBuffer(bytes));
                pos.add(parameters);
            }
            return pos;
        } catch (Exception e) {
            COMMON_LOG.error(e);
            return null;
        }
    }

}
