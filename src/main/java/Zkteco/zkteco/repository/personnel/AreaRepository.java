package Zkteco.zkteco.repository.personnel;

import Zkteco.zkteco.domain.personnel.Area;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {

    Optional<Area> findFirstByDefaultAreaTrueOrderByIdAsc();

    List<Area> findByIdIn(Iterable<Long> ids);
}
