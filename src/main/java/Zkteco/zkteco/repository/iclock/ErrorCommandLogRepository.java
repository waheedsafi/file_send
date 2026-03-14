package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.ErrorCommandLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorCommandLogRepository extends JpaRepository<ErrorCommandLog, Long> {
}
