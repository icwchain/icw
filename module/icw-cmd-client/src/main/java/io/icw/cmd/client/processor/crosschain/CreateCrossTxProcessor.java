package io.icw.cmd.client.processor.crosschain;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.crosschain.facade.CreateCrossTxReq;
import io.icw.base.basic.AddressTool;
import io.icw.cmd.client.CommandBuilder;
import io.icw.cmd.client.CommandResult;
import io.icw.cmd.client.utils.AssetsUtil;
import io.icw.core.constant.ErrorCode;
import io.icw.core.core.annotation.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:34
 * @Description: 功能描述
 */
@Component
public class CreateCrossTxProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "createcrosstx";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<formAddress>  source address - require")
                .newLine("\t<toAddress>  target address - require")
                .newLine("\t<assetChainId> transaction asset chainId - require")
                .newLine("\t<assetId> transaction assetId - require")
                .newLine("\t<amount> \t\tamount - required")
                .newLine("\t[remark] \t\tremark ")
                .newLine("\t[password] \t\tpassword");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createcrosstx <formAddress> <toAddress> <assetChainId> <assetId> <amount> [remark] [password]--create cross chain tx";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 5, 6,7);
        checkAddress(config.getChainId(), args[1]);
        checkArgs(AddressTool.getChainIdByAddress(args[2]) != config.getChainId(), ErrorCode.init("cc_0001").getMsg());
        checkIsNumeric(args[3], "assetChainId");
        checkIsNumeric(args[4], "assetId");
        checkIsAmount(args[5], "amount");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = config.getChainId();
        String formAddress = args[1];
        String toAddress = args[2];
        Integer assetChainId = Integer.parseInt(args[3]);
        Integer assetId = Integer.parseInt(args[4]);
//        update cmd cross tx amount decimal
        Integer decimalInt = AssetsUtil.getCrossAssetDecimal(assetChainId, assetId);
        if (null == decimalInt) {
            return CommandResult.getFailed("cross asset info not exist.");
        }
        BigDecimal decimal = BigDecimal.TEN.pow(decimalInt);
        BigInteger amount = new BigDecimal(args[5]).multiply(decimal).toBigInteger();
        String remark = null;
        String password = null;
        if (args.length == 7) {
            remark = args[6];
        }
        if (args.length == 8) {
            password = args[7];
        }else {
            password = getPwd();
        }
        Result<String> result = crossChainProvider.createCrossTx(
                new CreateCrossTxReq.CreateCrossTxReqBuilder(chainId)
                        .addForm(assetChainId, assetId, formAddress, password, amount)
                        .addTo(assetChainId, assetId, toAddress, amount)
                        .setRemark(remark).build());
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
