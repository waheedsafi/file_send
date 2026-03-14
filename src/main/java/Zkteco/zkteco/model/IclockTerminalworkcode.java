package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_terminalworkcode",
    indexes = {
        @Index(name = "idx_iclock_terminalworkcode_company_id", columnList = "company_id"),
        @Index(name = "idx_iclock_terminalworkcode_last_activity", columnList = "last_activity")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminalworkcode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "create_user", length = 150)
    private String createUser;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "change_user", length = 150)
    private String changeUser;

    @Column(name = "status", nullable = false)
    private Short status = 0;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "code", nullable = false, unique = true, length = 8)
    private String code;

    @Column(name = "alias", nullable = false, length = 24)
    private String alias;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
}
