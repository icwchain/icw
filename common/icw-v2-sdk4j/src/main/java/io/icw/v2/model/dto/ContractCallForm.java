package io.icw.v2.model.dto;

import io.icw.core.rpc.model.ApiModelProperty;

public class ContractCallForm extends ContractBaseForm {

    @ApiModelProperty(description = "智能合约地址", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "交易附带的货币量", required = false)
    private long value;
    @ApiModelProperty(description = "调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[<value>,<assetChainId>,<assetId>]]", required = false)
    private String[][] multyAssetValues;
    @ApiModelProperty(description = "方法名", required = true)
    private String methodName;
    @ApiModelProperty(description = "方法签名，如果方法名不重复，可以不传", required = false)
    private String methodDesc;
    @ApiModelProperty(description = "参数列表", required = false)
    private Object[] args;

    public String[][] getMultyAssetValues() {
        return multyAssetValues;
    }

    public void setMultyAssetValues(String[][] multyAssetValues) {
        this.multyAssetValues = multyAssetValues;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
