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
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "iclock_transactionproofcmd",
        indexes = {
                @Index(name = "idx_iclock_transactionproofcmd_terminal_id", columnList = "terminal_id"),
                @Index(name = "idx_iclock_transactionproofcmd_action_time", columnList = "action_time")
        }
)
public class TransactionProofCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_iclock_transactionproofcmd_terminal"))
    private Terminal terminal;

    @Column(name = "action_time", nullable = false)
    private OffsetDateTime actionTime;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "terminal_count")
    private Integer terminalCount;

    @Column(name = "server_count")
    private Integer serverCount;

    @Column(name = "flag")
    private Short flag = 0;

    @Column(name = "reserved_init")
    private Integer reservedInit = 0;

    @Column(name = "reserved_float")
    private Double reservedFloat = 0D;

    @Column(name = "reserved_char", length = 30)
    private String reservedChar = "0";

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

    public OffsetDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(OffsetDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getTerminalCount() {
        return terminalCount;
    }

    public void setTerminalCount(Integer terminalCount) {
        this.terminalCount = terminalCount;
    }

    public Integer getServerCount() {
        return serverCount;
    }

    public void setServerCount(Integer serverCount) {
        this.serverCount = serverCount;
    }

    public Short getFlag() {
        return flag;
    }

    public void setFlag(Short flag) {
        this.flag = flag;
    }

    public Integer getReservedInit() {
        return reservedInit;
    }

    public void setReservedInit(Integer reservedInit) {
        this.reservedInit = reservedInit;
    }

    public Double getReservedFloat() {
        return reservedFloat;
    }

    public void setReservedFloat(Double reservedFloat) {
        this.reservedFloat = reservedFloat;
    }

    public String getReservedChar() {
        return reservedChar;
    }

    public void setReservedChar(String reservedChar) {
        this.reservedChar = reservedChar;
    }
}
