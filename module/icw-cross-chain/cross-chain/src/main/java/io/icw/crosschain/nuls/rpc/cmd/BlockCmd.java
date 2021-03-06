package io.icw.crosschain.nuls.rpc.cmd;

import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.crosschain.nuls.servive.BlockService;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.rpc.model.CmdAnnotation;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.model.message.Response;
import io.icw.core.basic.Result;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;

import java.util.Map;

/**
 * 提供给区块模块调用的接口
 * @author tag
 * @date 2019/4/25
 */
@Component
public class BlockCmd extends BaseCmd {
    @Autowired
    private BlockService service;
    /**
     * 区块模块高度变化通知跨链模块
     * */
    @CmdAnnotation(cmd = "newBlockHeight", version = 1.0, description = "链区块高度变更/receive new block height")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "height", parameterType = "long", parameterDes = "链ID")
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response newBlockHeight(Map<String,Object> params){
        Result result = service.newBlockHeight(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 节点同步状态变更
     * Node synchronization state change
     * */
    @CmdAnnotation(cmd = "syncStatusUpdate", version = 1.0, description = "Node synchronization state change")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "status", parameterType = "int", parameterDes = "状态0：同步中，1：同步完成")
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response syncStatusUpdate(Map<String,Object> params){
        Result result = service.newBlockHeight(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
