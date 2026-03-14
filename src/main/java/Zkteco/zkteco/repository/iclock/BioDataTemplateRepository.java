package Zkteco.zkteco.repository.iclock;

import Zkteco.zkteco.domain.iclock.BioDataTemplate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BioDataTemplateRepository extends JpaRepository<BioDataTemplate, Long> {

    Optional<BioDataTemplate> findByEmployeeIdAndBioNoAndBioIndexAndBioTypeAndBioFormatAndMajorVer(
            Long employeeId,
            Integer bioNo,
            Integer bioIndex,
            Integer bioType,
            Integer bioFormat,
            String majorVer
    );

    List<BioDataTemplate> findByEmployeeIdAndBioTypeOrderByBioNoAscBioIndexAsc(Long employeeId, Integer bioType);

    List<BioDataTemplate> findByEmployeeIdAndBioTypeIn(Long employeeId, Collection<Integer> bioTypes);
}
