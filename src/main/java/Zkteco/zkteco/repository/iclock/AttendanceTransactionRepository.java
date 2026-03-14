package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.AttendanceTransaction;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceTransactionRepository extends JpaRepository<AttendanceTransaction, Long> {
    boolean existsByCompanyCodeAndEmpCodeAndPunchTime(String companyCode, String empCode, OffsetDateTime punchTime);

    boolean existsByTerminalSnAndEmpCodeAndPunchTime(String terminalSn, String empCode, OffsetDateTime punchTime);

    long countByTerminalSnAndPunchTimeBetween(String terminalSn, OffsetDateTime startTime, OffsetDateTime endTime);

    List<AttendanceTransaction> findTop200ByOrderByPunchTimeDescIdDesc();

    List<AttendanceTransaction> findTop200ByTerminalSnOrderByPunchTimeDescIdDesc(String terminalSn);

    List<AttendanceTransaction> findTop200ByEmpCodeOrderByPunchTimeDescIdDesc(String empCode);

    List<AttendanceTransaction> findTop200ByTerminalSnAndEmpCodeOrderByPunchTimeDescIdDesc(String terminalSn, String empCode);
}

