package Zkteco.zkteco.domain.iclock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "iclock_terminalparameter",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_iclock_terminalparameter_terminal_param", columnNames = {"terminal_id", "param_name"})
        },
        indexes = {
                @Index(name = "idx_iclock_terminalparameter_terminal_id", columnList = "terminal_id")
        }
)
public class TerminalParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_iclock_terminalparameter_terminal"))
    private Terminal terminal;

    @Column(name = "param_type", length = 10)
    private String paramType;

    @Column(name = "param_name", nullable = false, length = 30)
    private String paramName;

    @Column(name = "param_value", nullable = false, length = 100)
    private String paramValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
