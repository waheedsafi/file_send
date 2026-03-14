package Zkteco.zkteco.modules.iclock.dto;

import java.util.List;

public record TerminalBatchCommandResponse(
        Long terminalId,
        String terminalSn,
        String terminalAlias,
        List<TerminalCommandResponse> commands
) {
}
