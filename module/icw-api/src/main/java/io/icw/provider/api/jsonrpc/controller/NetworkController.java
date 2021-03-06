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
import io.icw.base.api.provider.network.NetworkProvider;
import io.icw.base.api.provider.network.facade.NetworkInfo;
import io.icw.core.core.annotation.Controller;
import io.icw.core.core.annotation.RpcMethod;
import io.icw.core.rpc.model.Key;
import io.icw.core.rpc.model.ResponseData;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.provider.model.jsonrpc.RpcResult;
import io.icw.provider.model.jsonrpc.RpcResultError;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;
import io.icw.v2.model.annotation.ApiType;

import java.util.List;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class NetworkController {

    private NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    @RpcMethod("getNetworkInfo")
    @ApiOperation(description = "????????????????????????????????????", order = 201)
    @ResponseData(name = "?????????", description = "??????????????????", responseType = @TypeDescriptor(value = NetworkInfo.class, mapKeys = {
            @Key(name = "localBestHeight", description = "????????????"),
            @Key(name = "netBestHeight", description = "????????????"),
            @Key(name = "timeOffset", description = "???????????????"),
            @Key(name = "inCount", description = "???????????????"),
            @Key(name = "outCount", description = "???????????????"),
    }))
    public RpcResult getVersion(List<Object> params) {
        Result<NetworkInfo> result = networkProvider.getInfo();
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        } else {
            rpcResult.setResult(result.getData());
        }
        return rpcResult;
    }
}
