package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_terminalparameter",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_iclock_terminalparameter_terminal_param", columnNames = {"terminal_id", "param_name"})
    },
    indexes = {
        @Index(name = "idx_iclock_terminalparameter_terminal_id", columnList = "terminal_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminalparameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "param_type", length = 10)
    private String paramType;

    @Column(name = "param_name", nullable = false, length = 30)
    private String paramName;

    @Column(name = "param_value", nullable = false, length = 100)
    private String paramValue;
}
