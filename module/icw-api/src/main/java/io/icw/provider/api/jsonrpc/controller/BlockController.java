/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.icw.provider.api.jsonrpc.controller;

import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.block.BlockService;
import io.icw.base.api.provider.block.facade.BlockHeaderData;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.icw.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.icw.base.data.Block;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Controller;
import io.icw.core.core.annotation.RpcMethod;
import io.icw.core.exception.NulsException;
import io.icw.provider.model.dto.block.BlockDto;
import io.icw.provider.model.dto.block.BlockHeaderDto;
import io.icw.core.rpc.model.*;
import io.icw.provider.api.config.Config;
import io.icw.provider.api.config.Context;
import io.icw.provider.api.manager.BeanCopierManager;
import io.icw.provider.model.jsonrpc.RpcResult;
import io.icw.provider.rpctools.BlockTools;
import io.icw.provider.utils.Log;
import io.icw.provider.utils.ResultUtil;
import io.icw.provider.utils.VerifyUtils;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;
import io.icw.v2.model.annotation.ApiType;
import io.icw.v2.util.ValidateUtil;

import java.util.List;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class BlockController {

    BlockService blockService = ServiceManager.get(BlockService.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("getHeaderByHeight")
    @ApiOperation(description = "?????????????????????????????????", order = 201)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "????????????")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }

        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        GetBlockHeaderByHeightReq req = new GetBlockHeaderByHeightReq(height);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHeight(req);
        if (result.isSuccess() && result.getData() != null) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getHeaderByHash")
    @ApiOperation(description = "????????????hash???????????????", order = 202)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "hash", parameterDes = "??????hash")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getHeaderByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is required");
        }
        GetBlockHeaderByHashReq req = new GetBlockHeaderByHashReq(hash);
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByHash(req);
        if (result.isSuccess() && result.getData() != null) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlockHeader")
    @ApiOperation(description = "???????????????????????????", order = 203)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockHeaderDto.class))
    public RpcResult getBestBlockHeader(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        GetBlockHeaderByLastHeightReq req = new GetBlockHeaderByLastHeightReq();
        req.setChainId(config.getChainId());
        Result<BlockHeaderData> result = blockService.getBlockHeaderByLastHeight(req);
        if (result.isSuccess()) {
            BlockHeaderData data = result.getData();
            BlockHeaderDto dto = new BlockHeaderDto();
            BeanCopierManager.beanCopier(data, dto);
            return RpcResult.success(dto);
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBestBlock")
    @ApiOperation(description = "??????????????????", order = 204, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBestBlock(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBestBlock(Context.getChainId());
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                BeanCopierManager.beanCopier(data, dto);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    @RpcMethod("getBlockByHeight")
    @ApiOperation(description = "??????????????????????????????", order = 205, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "????????????")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHeight(chainId, height);
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockByHash")
    @ApiOperation(description = "????????????hash????????????", order = 206, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "hash", parameterDes = "??????hash")
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = BlockDto.class))
    public RpcResult getBlockByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<Block> result = blockTools.getBlockByHash(chainId, hash);
        if (result.isSuccess() && result.getData() != null) {
            Block data = result.getData();
            try {
                BlockDto dto = new BlockDto(data);
                return RpcResult.success(dto);
            } catch (NulsException e) {
                Log.error(e);
                return ResultUtil.getNulsExceptionJsonRpcResult(e);
            }
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockSerializationByHeight")
    @ApiOperation(description = "????????????????????????????????????????????????", order = 207, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "????????????")
    })
    @ResponseData(name = "?????????", description = "???????????????????????????HEX?????????", responseType = @TypeDescriptor(value = String.class))
    public RpcResult getBlockSerializationByHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        long height;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            height = Long.parseLong("" + params.get(1));
        } catch (Exception e) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (height < 0) {
            return RpcResult.paramError("[height] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.getBlockSerializationByHeight(chainId, height);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getBlockSerializationByHash")
    @ApiOperation(description = "????????????hash??????????????????????????????", order = 208, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID"),
            @Parameter(parameterName = "hash", parameterDes = "??????hash")
    })
    @ResponseData(name = "?????????", description = "???????????????????????????HEX?????????", responseType = @TypeDescriptor(value = String.class))
    public RpcResult getBlockSerializationByHash(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String hash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            hash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!ValidateUtil.validHash(hash)) {
            return RpcResult.paramError("[hash] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.getBlockSerializationByHash(chainId, hash);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getLatestHeight")
    @ApiOperation(description = "????????????????????????", order = 209)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "???ID")
    })
    @ResponseData(name = "?????????", description = "????????????????????????", responseType = @TypeDescriptor(value = Long.class))
    public RpcResult getLatestHeight(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        Result<String> result = blockTools.latestHeight(chainId);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("testGetBlock")
    public RpcResult testGetBlock(List<Object> params) {
        while (true) {
            Result<Block> result = blockTools.getBestBlock(1);
            if(result.isFailed()) {
                System.out.println(result.getStatus());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
