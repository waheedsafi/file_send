package Zkteco.zkteco.repository.common;

import Zkteco.zkteco.domain.common.AsyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {
}
