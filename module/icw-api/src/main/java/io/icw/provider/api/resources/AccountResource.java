/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.icw.provider.api.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.icw.provider.model.dto.AccountKeyStoreDto;
import io.icw.base.api.provider.account.facade.*;
import io.icw.provider.api.config.Config;
import io.icw.base.api.provider.Result;
import io.icw.base.api.provider.ServiceManager;
import io.icw.base.api.provider.account.AccountService;
import io.icw.base.basic.AddressTool;
import io.icw.core.constant.CommonCodeConstanst;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.crypto.HexUtil;
import io.icw.core.model.FormatValidUtils;
import io.icw.core.model.StringUtils;
import io.icw.core.parse.JSONUtils;
import io.icw.core.rpc.model.*;
import io.icw.provider.model.ErrorData;
import io.icw.provider.model.RpcClientResult;
import io.icw.provider.model.form.*;
import io.icw.provider.rpctools.AccountTools;
import io.icw.provider.utils.Log;
import io.icw.provider.utils.ResultUtil;
import io.icw.v2.error.AccountErrorCode;
import io.icw.v2.model.annotation.Api;
import io.icw.v2.model.annotation.ApiOperation;
import io.icw.v2.model.dto.AccountDto;
import io.icw.v2.model.dto.AliasDto;
import io.icw.v2.model.dto.MultiSignAliasDto;
import io.icw.v2.util.NulsSDKTool;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/account")
@Component
@Api
public class AccountResource {

    @Autowired
    Config config;

    AccountService accountService = ServiceManager.get(AccountService.class);
    @Autowired
    private AccountTools accountTools;


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????", order = 101, detailDesc = "???????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "count", parameterDes = "??????????????????,??????[1-10000]"),
            @Parameter(parameterName = "password", parameterDes = "????????????")
    })
    @ResponseData(name = "?????????", description = "????????????Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = String.class, description = "????????????")
    }))
    public RpcClientResult create(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (form.getCount() <= 0 || form.getCount() > 10000) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[count] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }

        CreateAccountReq req = new CreateAccountReq(form.getCount(), form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.createAccount(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("list", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @PUT
    @Path("/password/{address}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(description = "??????????????????", order = 102)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "????????????"),
            @Parameter(parameterName = "form", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = AccountUpdatePasswordForm.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "??????????????????")
    }))
    public RpcClientResult updatePassword(@PathParam("address") String address, AccountUpdatePasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (!AddressTool.validAddress(config.getChainId(), address)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[address] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getNewPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[newPassword] is invalid"));
        }
        UpdatePasswordReq req = new UpdatePasswordReq(address, form.getPassword(), form.getNewPassword());
        req.setChainId(config.getChainId());
        Result<Boolean> result = accountService.updatePassword(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/prikey/{address}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(description = "??????????????????", order = 103, detailDesc = "????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "????????????"),
            @Parameter(parameterName = "form", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = AccountPasswordForm.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "??????")
    }))
    public RpcClientResult getPriKey(@PathParam("address") String address, AccountPasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        GetAccountPrivateKeyByAddressReq req = new GetAccountPrivateKeyByAddressReq(form.getPassword(), address);
        req.setChainId(config.getChainId());
        Result<String> result = accountService.getAccountPrivateKey(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????", order = 104, detailDesc = "?????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = AccountPriKeyPasswordForm.class))
    })
    @ResponseData(name = "?????????", description = "??????????????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult importPriKey(AccountPriKeyPasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }
        ImportAccountByPrivateKeyReq req = new ImportAccountByPrivateKeyReq(form.getPassword(), form.getPriKey(), form.getOverwrite());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByPrivateKey(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/keystore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(description = "??????keyStore????????????", order = 105)
    @Parameters({
            @Parameter(parameterName = "????????????????????????", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = InputStream.class))
    })
    @ResponseData(name = "?????????", description = "??????????????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult importAccountByKeystoreFile(@FormDataParam("keystore") InputStream in,
                                                       @FormDataParam("password") String password) {
        if (in == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "inputStream is empty"));
        }
        Result<AccountKeyStoreDto> dtoResult = this.getAccountKeyStoreDto(in);
        if (dtoResult.isFailed()) {
            return RpcClientResult.getFailed(new ErrorData(dtoResult.getStatus(), dtoResult.getMessage()));
        }
        AccountKeyStoreDto dto = dtoResult.getData();
        try {
            ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(password, HexUtil.encode(JSONUtils.obj2json(dto).getBytes()), true);
            req.setChainId(config.getChainId());
            Result<String> result = accountService.importAccountByKeyStore(req);
            RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
            if (clientResult.isSuccess()) {
                return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
            }
            return clientResult;
        } catch (JsonProcessingException e) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage()));
        }
    }

    @POST
    @Path("/import/keystore/path")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????keystore????????????????????????", order = 106)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????keystore??????????????????????????????", requestType = @TypeDescriptor(value = AccountKeyStoreImportForm.class))
    })
    @ResponseData(name = "?????????", description = "??????????????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult importAccountByKeystoreFilePath(AccountKeyStoreImportForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        String keystore = accountService.getAccountKeystoreDto(form.getPath());
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), true);
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByKeyStore(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/keystore/json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????keystore?????????????????????", order = 107)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????keystore???????????????????????????", requestType = @TypeDescriptor(value = AccountKeyStoreJsonImportForm.class))
    })
    @ResponseData(name = "?????????", description = "??????????????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult importAccountByKeystoreJson(AccountKeyStoreJsonImportForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        String keystore = null;
        try {
            keystore = JSONUtils.obj2json(form.getKeystore());
        } catch (JsonProcessingException e) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "keystore is invalid"));
        }
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), true);
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByKeyStore(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/export/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "?????????????????????AccountKeyStore?????????????????????", order = 108)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "????????????", requestType = @TypeDescriptor(value = String.class)),
            @Parameter(parameterName = "form", parameterDes = "keystone??????????????????", requestType = @TypeDescriptor(value = AccountKeyStoreBackup.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "path", description = "?????????????????????")
    }))
    public RpcClientResult exportAccountKeyStore(@PathParam("address") String address, AccountKeyStoreBackup form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        BackupAccountReq req = new BackupAccountReq(form.getPassword(), address, form.getPath());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.backupAccount(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("path", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    private Result<AccountKeyStoreDto> getAccountKeyStoreDto(InputStream in) {
        StringBuilder ks = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String str;
        try {
            inputStreamReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((str = bufferedReader.readLine()) != null) {
                if (!str.isEmpty()) {
                    ks.append(str);
                }
            }
            AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
            return new Result(accountKeyStoreDto);
        } catch (Exception e) {
            return Result.fail(CommonCodeConstanst.FILE_OPERATION_FAILD.getCode(), "key store file error");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
    }

    @POST
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????", order = 109, detailDesc = "???????????????1-20?????????????????????????????????????????????????????????1???NULS")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = SetAliasForm.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "?????????????????????hash")
    }))
    public RpcClientResult setAlias(SetAliasForm form) {
        if (!AddressTool.validAddress(config.getChainId(), form.getAddress())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is invalid"));
        }
        if (!FormatValidUtils.validAlias(form.getAlias())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "alias is invalid"));
        }
        if (StringUtils.isBlank(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "password is invalid"));
        }
        SetAccountAliasReq aliasReq = new SetAccountAliasReq(form.getPassword(), form.getAddress(), form.getAlias());
        Result<String> result = accountService.setAccountAlias(aliasReq);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/address/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????????????????", order = 110, detailDesc = "??????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = ValidateAddressForm.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "true")
    }))
    public RpcClientResult validateAddress(ValidateAddressForm form) {
        boolean b = AddressTool.validAddress(form.getChainId(), form.getAddress());
        if (b) {
            Map map = new HashMap();
            map.put("value", true);
            return RpcClientResult.getSuccess(map);
        } else {
            return RpcClientResult.getFailed(new ErrorData(AccountErrorCode.ADDRESS_ERROR.getCode(), "address is wrong"));
        }
    }

    @POST
    @Path("/address/publickey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????????????????", order = 111, detailDesc = "????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????????????????????????????", requestType = @TypeDescriptor(value = AccountPublicKeyForm.class))
    })
    @ResponseData(name = "?????????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "????????????")
    }))
    public RpcClientResult getAddressByPublicKey(AccountPublicKeyForm form) {
        try {
            byte[] address = AddressTool.getAddress(HexUtil.decode(form.getPublicKey()), form.getChainId());
            return RpcClientResult.getSuccess(Map.of("address", AddressTool.getStringAddressByBytes(address)));
        } catch (Exception e) {
            Log.error(e);
            return RpcClientResult.getFailed(new ErrorData(AccountErrorCode.ADDRESS_ERROR.getCode(), "address is wrong"));
        }
    }

    @POST
    @Path("/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "?????? - ??????????????????", order = 151, detailDesc = "???????????????????????????????????????,???????????????????????????keystore??????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = AccountCreateForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = AccountDto.class, description = "??????keystore??????")
    }))
    public RpcClientResult createOffline(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.icw.core.basic.Result<List<AccountDto>> result;
        if (StringUtils.isBlank(form.getPrefix())) {
            result = NulsSDKTool.createOffLineAccount(form.getCount(), form.getPassword());
        } else {
            result = NulsSDKTool.createOffLineAccount(form.getChainId(), form.getCount(), form.getPrefix(), form.getPassword());
        }
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????????????????", order = 152)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????????????????????????????", requestType = @TypeDescriptor(value = GetPriKeyForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult getPriKeyOffline(GetPriKeyForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.getPriKeyOffline(form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @PUT
    @Path("/password/offline/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????", order = 153)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = ResetPasswordForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "??????????????????????????????")
    }))
    public RpcClientResult resetPasswordOffline(ResetPasswordForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.resetPasswordOffline(form.getAddress(), form.getEncryptedPriKey(), form.getOldPassword(), form.getNewPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multi/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "?????????????????????", order = 154, detailDesc = "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "???????????????????????????", requestType = @TypeDescriptor(value = MultiSignForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "??????????????????16???????????????")
    }))
    public RpcClientResult multiSign(MultiSignForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.sign(form.getDtoList(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????", order = 155)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = PriKeySignForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "??????????????????16???????????????")
    }))
    public RpcClientResult priKeySign(PriKeySignForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getPriKey());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/encryptedPriKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????", order = 156)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = EncryptedPriKeySignForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "??????????????????16???????????????")
    }))
    public RpcClientResult encryptedPriKeySign(EncryptedPriKeySignForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/encryptedPriKeys/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "?????????????????????????????????", order = 156)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "??????????????????????????????", requestType = @TypeDescriptor(value = EncryptedPriKeySignForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "??????????????????16???????????????")
    }))
    public RpcClientResult encryptedPriKeysSign(EncryptedPriKeysSignForm form) {
//        Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
//        return ResultUtil.getRpcClientResult(result);
//        return null;
        io.icw.core.basic.Result result = NulsSDKTool.sign(form.getChainId(), form.getPrefix(), form.getSignDtoList(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????", order = 157, detailDesc = "????????????????????????????????????????????????minSigns??????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = MultiSignAccountCreateForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "???????????????")
    }))
    public RpcClientResult createMultiSignAccount(MultiSignAccountCreateForm form) {
        if (form.getPubKeys() == null || form.getPubKeys().isEmpty()) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "pubKeys is empty"));
        }
        if (form.getMinSigns() < 1 || form.getMinSigns() > form.getPubKeys().size()) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[minSigns] is invalid"));
        }
        io.icw.core.basic.Result result = NulsSDKTool.createMultiSignAccount(form.getPubKeys(), form.getMinSigns());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/aliasTx/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????????????????", order = 158, detailDesc = "????????????????????????????????????????????????minSigns??????????????????????????????????????????????????????")
    @Parameters({
            @Parameter(parameterName = "dto", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = AliasDto.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "???????????????16???????????????")
    }))
    public RpcClientResult createAliasTxOffLine(AliasDto dto) {
        io.icw.core.basic.Result result = NulsSDKTool.createAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/aliasTx/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "??????????????????????????????????????????", order = 159)
    @Parameters({
            @Parameter(parameterName = "dto", parameterDes = "????????????????????????", requestType = @TypeDescriptor(value = MultiSignAliasDto.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "??????hash"),
            @Key(name = "txHex", description = "???????????????16???????????????")
    }))
    public RpcClientResult createMultiSignAliasTxOffLine(MultiSignAliasDto dto) {
        io.icw.core.basic.Result result = NulsSDKTool.createMultiSignAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/address/priKey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "????????????????????????????????????", order = 160)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "????????????", requestType = @TypeDescriptor(value = PriKeyForm.class))
    })
    @ResponseData(name = "?????????", description = "????????????Map??????", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "????????????")
    }))
    public RpcClientResult getAddressByPriKey(PriKeyForm form) {
        io.icw.core.basic.Result result = NulsSDKTool.getAddressByPriKey(form.getPriKey());
        return ResultUtil.getRpcClientResult(result);
    }
}
