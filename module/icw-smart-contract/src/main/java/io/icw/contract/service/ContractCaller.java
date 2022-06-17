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

package io.icw.contract.service;

import io.icw.contract.model.bo.ContractContainer;
import io.icw.contract.model.bo.ContractResult;
import io.icw.contract.model.bo.ContractWrapperTransaction;
import io.icw.contract.vm.program.ProgramExecutor;
import io.icw.core.basic.Result;
import io.icw.core.exception.NulsException;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public interface ContractCaller {

    Result callTx(int chainId, ContractContainer executorServiceContainer, ProgramExecutor batchExecutor, ContractWrapperTransaction tx, String preStateRoot);

    Result callBatchEnd(int chainId, long blockHeight);

    List<ContractResult> reCallTx(ProgramExecutor batchExecutor, List<ContractWrapperTransaction> reCallTxList, int chainId, String preStateRoot) throws NulsException;

}