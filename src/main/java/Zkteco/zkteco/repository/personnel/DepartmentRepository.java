package Zkteco.zkteco.repository.personnel;

import Zkteco.zkteco.domain.personnel.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findFirstByCompanyIdOrderByIdAsc(Long companyId);

    Optional<Department> findFirstByOrderByIdAsc();
}
