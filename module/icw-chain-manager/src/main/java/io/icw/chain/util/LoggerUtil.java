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
package io.icw.chain.util;

import io.icw.core.log.logback.LoggerBuilder;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.rpc.model.ModuleE;

/**
 * @author lan
 * @description
 * @date 2018/12/17
 **/
public class LoggerUtil {
    private static NulsLogger logger = null;
    public static final NulsLogger COMMON_LOG = LoggerBuilder.getLogger(ModuleE.Constant.CHAIN_MANAGER);

    public static void defaultLogInit(int chainId) {
        logger = LoggerBuilder.getLogger(ModuleE.Constant.CHAIN_MANAGER, chainId);
    }

    public static NulsLogger logger() {
        return logger;
    }
}
