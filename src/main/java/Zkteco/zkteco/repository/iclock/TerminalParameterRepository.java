package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.TerminalParameter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalParameterRepository extends JpaRepository<TerminalParameter, Long> {

    Optional<TerminalParameter> findByTerminalIdAndParamName(Long terminalId, String paramName);
}
