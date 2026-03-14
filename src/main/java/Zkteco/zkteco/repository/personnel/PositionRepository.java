package Zkteco.zkteco.repository.personnel;

import Zkteco.zkteco.domain.personnel.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
}
