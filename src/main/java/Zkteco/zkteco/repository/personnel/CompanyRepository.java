package Zkteco.zkteco.repository.personnel;

import Zkteco.zkteco.domain.personnel.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCompanyCode(String companyCode);
}
