package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.Terminal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalRepository extends JpaRepository<Terminal, Long> {

    Optional<Terminal> findBySn(String sn);

    List<Terminal> findBySnInOrderByIdAsc(Collection<String> sns);

    List<Terminal> findDistinctByAreaIdInOrderByIdAsc(Collection<Long> areaIds);
}
