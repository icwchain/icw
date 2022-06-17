package io.icw.poc.model.dto.output;

import io.icw.base.basic.AddressTool;
import io.icw.poc.utils.enumeration.PunishReasonEnum;
import io.icw.poc.model.po.PunishLogPo;
import io.icw.core.rpc.util.NulsDateUtils;

import java.util.Date;

/**
 * 惩罚信息类
 * Punishment information category
 *
 * @author tag
 * @date: 2018/11/20
 */
public class PunishLogDTO {

    private byte type;
    private String address;
    private String time;
    private long height;
    private long roundIndex;
    private String reasonCode;

    public PunishLogDTO(PunishLogPo po) {
        this.type = po.getType();
        this.address = AddressTool.getStringAddressByBytes(po.getAddress());
        this.time = NulsDateUtils.convertDate(new Date(po.getTime()));
        this.height = po.getHeight();
        this.roundIndex = po.getRoundIndex();
        this.reasonCode = PunishReasonEnum.getEnum(po.getReasonCode()).getMessage();
    }

    public byte getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getTime() {
        return time;
    }

    public long getHeight() {
        return height;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public String getReasonCode() {
        return reasonCode;
    }
}
