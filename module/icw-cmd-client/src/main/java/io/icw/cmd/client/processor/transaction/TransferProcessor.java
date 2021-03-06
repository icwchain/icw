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

package io.icw.cmd.client.processor.transaction;


import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.transaction.facade.TransferReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author: zhoulijun
 */
@Component
public class TransferProcessor extends TransactionBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "transfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address or alias - Required")
                .newLine("\t<toaddress> \treceiving address or alias - Required")
                .newLine("\t<amount> \t\tamount - Required")
                .newLine("\t[remark] \t\tremark - ")
                .newLine("\t[password] \t\tpassword - ");

        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transfer <address>|<alias> <toAddress>|<alias> <amount> [remark] [password] --transfer";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,3,4);
        checkIsAmount(args[3],"amount");
        return true;
    }

    private TransferReq buildTransferReq(String[] args) {
        String formAddress = args[1];
        String toAddress = args[2];
        BigInteger amount = config.toSmallUnit(new BigDecimal(args[3]));
        String password;
        if(args.length == 6){
            password = args[5];
        }else{
            password = getPwd("\nEnter your account password:");
        }
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(),config.getAssetsId())
                        .addForm(formAddress,password, amount)
                        .addTo(toAddress,amount);
        if(args.length == 5){
            builder.setRemark(args[4]);
        }
        return builder.build(new TransferReq());
    }

    @Override
    public CommandResult execute(String[] args) {
        Result<String> result = transferService.transfer(buildTransferReq(args));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
