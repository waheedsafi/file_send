package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.TerminalUploadLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalUploadLogRepository extends JpaRepository<TerminalUploadLog, Long> {

    List<TerminalUploadLog> findTop200ByOrderByUploadTimeDescIdDesc();

    List<TerminalUploadLog> findTop200ByTerminal_SnOrderByUploadTimeDescIdDesc(String sn);
}
