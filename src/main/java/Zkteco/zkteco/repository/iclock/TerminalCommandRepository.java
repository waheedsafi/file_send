package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.TerminalCommand;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalCommandRepository extends JpaRepository<TerminalCommand, Long> {

    Optional<TerminalCommand> findTopByTerminalIdAndTransferTimeIsNullAndReturnTimeIsNullOrderByIdAsc(Long terminalId);

    java.util.List<TerminalCommand> findByTerminalIdOrderByIdAsc(Long terminalId);
}
