package io.icw.cmd.client.processor.crosschain;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.crosschain.facade.CrossChainRegisterInfo;
import io.icw.base.api.provider.crosschain.facade.GetCrossChainInfoReq;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:41
 * @Description: 功能描述
 */
@Component
public class GetCrossChainRegisterInfoProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "crosschaininfo";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<chainId>  register chain id - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <chainId> chain id - require";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkIsNumeric(args[1],"chainId");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = Integer.parseInt(args[1]);
        Result<CrossChainRegisterInfo> result = chainManageProvider.getCrossChainInfo(new GetCrossChainInfoReq(chainId));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
