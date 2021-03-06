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
import io.icw.base.api.provider.account.facade.UpdatePasswordReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandHelper;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.processor.CommandProcessor;
import io.icw.core.core.annotation.Component;

/**
 * @author zhoulijun
 * 修改密码
 * update account password
 */
@Component
public class UpdatePasswordProcessor extends AccountBaseProcessor implements CommandProcessor {


    @Override
    public String getCommand() {
        return "resetpwd";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address of the account - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "resetpwd <address> --reset password for account";
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
        String password = CommandHelper.getPwd( "Enter your old password:");
        String newPassword = CommandHelper.getPwd("Enter new password:");
        if(!CommandHelper.confirmPwd(newPassword)) {
            return CommandResult.getFailed("Password confirmation doesn't match the password.Operation abort.");
        }
        UpdatePasswordReq req = new UpdatePasswordReq(address,password,newPassword);
        Result<Boolean> res = accountService.updatePassword(req);
        if(res.isFailed()){
            return CommandResult.getFailed(res);
        }
        return CommandResult.getSuccess("Success");
    }
}
