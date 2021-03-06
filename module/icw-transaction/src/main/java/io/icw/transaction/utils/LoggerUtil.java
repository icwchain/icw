/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.transaction.utils;

import io.icw.core.log.logback.LoggerBuilder;
import io.icw.core.log.logback.NulsLogger;
import io.icw.core.rpc.model.ModuleE;
import io.icw.transaction.model.bo.Chain;

/**
 * @author: Charlie
 * @date: 2019/2/28
 */
public class LoggerUtil {

    public static final NulsLogger LOG = LoggerBuilder.getLogger(ModuleE.TX.name);

    public static void init(Chain chain){
        int chainId = chain.getConfig().getChainId();
        NulsLogger logger = LoggerBuilder.getLogger(ModuleE.TX.name, chainId);
        chain.setLogger(logger);
    }
}
