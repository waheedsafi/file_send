package Zkteco.zkteco.repository.personnel;

import Zkteco.zkteco.domain.personnel.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByCompanyIdAndEmpCode(Long companyId, String empCode);

    Optional<Employee> findFirstByEmpCodeOrderByIdAsc(String empCode);

    boolean existsByCompanyIdAndEmpCode(Long companyId, String empCode);

    List<Employee> findByCompanyIdOrderByIdAsc(Long companyId);

    List<Employee> findByCompanyIdAndEmpCodeContainingIgnoreCaseOrderByIdAsc(Long companyId, String empCode);
}
