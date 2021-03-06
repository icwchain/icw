package io.icw.v2.service;

import io.icw.v2.model.dto.BlockDto;
import io.icw.v2.model.dto.BlockHeaderDto;
import io.icw.v2.model.dto.RestFulResult;
import io.icw.base.data.BlockHeader;
import io.icw.core.basic.Result;
import io.icw.core.constant.ErrorCode;
import io.icw.v2.util.RestFulUtil;

import java.util.Map;

import static io.icw.v2.util.ValidateUtil.validateChainId;


public class BlockService {

    private BlockService() {

    }

    private static BlockService instance = new BlockService();

    public static BlockService getInstance() {
        return instance;
    }

    public Result getBlockHeader(long height) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/header/height/" + height);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockHeaderDto dto = BlockHeaderDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getBlockHeader(String hash) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/header/hash/" + hash);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockHeaderDto dto = BlockHeaderDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getBestBlockHeader() {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/header/newest");
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockHeaderDto dto = BlockHeaderDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getBlock(long height) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/height/" + height);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockDto dto = BlockDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getBlock(String hash) {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/hash/" + hash);
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockDto dto = BlockDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getBestBlock() {
        validateChainId();
        RestFulResult restFulResult = RestFulUtil.get("api/block/newest");
        Result result;
        if (restFulResult.isSuccess()) {
            result = Result.getSuccess(null);
            Map<String, Object> map = (Map<String, Object>) restFulResult.getData();
            BlockDto dto = BlockDto.mapToPojo(map);
            result.setData(dto);
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }

    public Result getInfo() {
        RestFulResult restFulResult = RestFulUtil.get("api/info");
        Result result;
        if (restFulResult.isSuccess()) {
            result = io.icw.core.basic.Result.getSuccess(restFulResult.getData());
        } else {
            ErrorCode errorCode = ErrorCode.init(restFulResult.getError().getCode());
            result = io.icw.core.basic.Result.getFailed(errorCode).setMsg(restFulResult.getError().getMessage());
        }
        return result;
    }
}
