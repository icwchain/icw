package io.icw.provider.model.form;

import io.icw.core.rpc.model.ApiModel;
import io.icw.core.rpc.model.ApiModelProperty;

@ApiModel(description = "账户私钥表单")
public class PriKeyForm {

    @ApiModelProperty(description = "账户明文私钥")
    private String priKey;

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }
}
