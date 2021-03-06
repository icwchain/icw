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

package io.icw.cmd.client.processor.account;


import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.account.facade.RemoveAccountReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandHelper;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.config.Config;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

/**
 * @author: zhoulijun
 */
@Component
public class RemoveAccountProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "remove";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> The account address - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "remove <address> --remove an account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkAddress(config.getChainId(),args[1]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String password = CommandHelper.getPwd();
        Result<Boolean> result = accountService.removeAccount(new RemoveAccountReq(password,address));
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess("Success");
    }
}
