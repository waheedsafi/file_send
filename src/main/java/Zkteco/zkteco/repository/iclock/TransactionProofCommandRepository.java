package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.TransactionProofCommand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionProofCommandRepository extends JpaRepository<TransactionProofCommand, Long> {

    List<TransactionProofCommand> findTop200ByOrderByActionTimeDescIdDesc();

    List<TransactionProofCommand> findTop200ByTerminal_SnOrderByActionTimeDescIdDesc(String sn);
}
