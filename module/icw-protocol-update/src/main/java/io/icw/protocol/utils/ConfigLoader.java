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

package io.icw.protocol.utils;

import io.icw.base.basic.ProtocolVersion;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.io.IoUtils;
import io.icw.core.log.Log;
import io.icw.core.parse.JSONUtils;
import io.icw.protocol.ProtocolUpdateBootstrap;
import io.icw.protocol.constant.Constant;
import io.icw.protocol.storage.ParametersStorageService;
import io.icw.protocol.manager.ContextManager;
import io.icw.protocol.model.ChainParameters;

import java.util.List;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
@Component
public class ConfigLoader {

    @Autowired
    private static ParametersStorageService service;
    private static List<ProtocolVersion> versions;

    static {
        try {
            versions = JSONUtils.json2list(IoUtils.read(Constant.PROTOCOL_CONFIG_FILE), ProtocolVersion.class);
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }


    /**
     * 加载配置文件
     *
     */
    public static void load() {
        List<ChainParameters> list = service.getList();
        if (list == null || list.size() == 0) {
            loadDefault();
        } else {
            for (ChainParameters chainParameters : list) {
                ContextManager.init(chainParameters, versions);
            }
        }
    }

    /**
     * 加载默认配置文件
     *
     */
    private static void loadDefault() {
        int chainId = ProtocolUpdateBootstrap.protocolConfig.getChainId();
        ContextManager.init(ProtocolUpdateBootstrap.protocolConfig, versions);
        service.save(ProtocolUpdateBootstrap.protocolConfig, chainId);
    }

}
