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

package io.icw.cmd.client.processor.consensus;


import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.consensus.ConsensusProvider;
import io.icw.base.api.provider.consensus.facade.MultiSignAccountWithdrawReq;
import io.icw.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.icw.base.data.NulsHash;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

/**
 * @author: zhoulijun
 * 撤销多签账户的委托共识
 */
@Component
public class WithdrawForMultiSignProcessor extends ConsensusBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Override
    public String getCommand() {
        return "withdrawformultisign";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   address -required")
                .newLine("\t<txHash>    your deposit transaction hash  -required")
                .newLine("\t[sign address] first sign address -- not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "withdrawformultisign <address> <txHash> [sign address]-- withdraw the agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2,3);
        checkAddress(config.getChainId(),args[1]);
        checkArgs(NulsHash.validHash(args[2]),"txHash format error");
        if(args.length == 4){
            checkAddress(config.getChainId(),args[3]);
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String txHash = args[2];
        MultiSignAccountWithdrawReq req = new MultiSignAccountWithdrawReq(address,txHash);
        if(args.length == 4){
            String signAddress = args[3];
            String password = getPwd();
            req.setSignAddress(signAddress);
            req.setPassword(password);
        }
        Result<MultiSignTransferRes> result = consensusProvider.withdrawForMultiSignAccount(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
