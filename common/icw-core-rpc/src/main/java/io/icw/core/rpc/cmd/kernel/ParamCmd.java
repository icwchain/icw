package io.icw.core.rpc.cmd.kernel;

import io.icw.core.rpc.model.message.Response;
import io.icw.core.rpc.info.Constants;
import io.icw.core.rpc.model.CmdAnnotation;
import io.icw.core.rpc.model.Parameter;
import io.icw.core.rpc.cmd.BaseCmd;
import io.icw.core.core.annotation.Component;

import java.util.Map;

/**
 * 测试接口，测试rpc服务稳定性
 *
 * @author tag
 * @version 1.0
 * @date 19-1-22
 */

@Component
public class ParamCmd extends BaseCmd {
    @CmdAnnotation(cmd = "paramTestCmd", version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "intCount", parameterType = "int", parameterValidRange = "[0,65535]")
    @Parameter(parameterName = "byteCount", parameterType = "byte", parameterValidRange = "[-128,127]")
    @Parameter(parameterName = "shortCount", parameterType = "short", parameterValidRange = "[0,32767]")
    @Parameter(parameterName = "longCount", parameterType = "long", parameterValidRange = "[0,55555555]")
    public Response paramTest(Map map) {
        int count = Integer.parseInt(map.get("intCount").toString());
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += i;
        }
        return success(sum);
    }
}
