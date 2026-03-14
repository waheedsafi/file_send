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
        name = "iclock_terminallog",
        indexes = {
                @Index(name = "idx_iclock_terminallog_terminal_id", columnList = "terminal_id"),
                @Index(name = "idx_iclock_terminallog_action_time", columnList = "action_time"),
                @Index(name = "idx_iclock_terminallog_upload_time", columnList = "upload_time")
        }
)
public class TerminalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_iclock_terminallog_terminal"))
    private Terminal terminal;

    @Column(name = "terminal_tz")
    private Short terminalTz;

    @Column(name = "admin", length = 50)
    private String admin;

    @Column(name = "action_name")
    private Short actionName;

    @Column(name = "action_time")
    private OffsetDateTime actionTime;

    @Column(name = "object", length = 50)
    private String object;

    @Column(name = "param1")
    private Integer param1;

    @Column(name = "param2")
    private Integer param2;

    @Column(name = "param3")
    private Integer param3;

    @Column(name = "upload_time")
    private OffsetDateTime uploadTime;

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

    public Short getTerminalTz() {
        return terminalTz;
    }

    public void setTerminalTz(Short terminalTz) {
        this.terminalTz = terminalTz;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Short getActionName() {
        return actionName;
    }

    public void setActionName(Short actionName) {
        this.actionName = actionName;
    }

    public OffsetDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(OffsetDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Integer getParam1() {
        return param1;
    }

    public void setParam1(Integer param1) {
        this.param1 = param1;
    }

    public Integer getParam2() {
        return param2;
    }

    public void setParam2(Integer param2) {
        this.param2 = param2;
    }

    public Integer getParam3() {
        return param3;
    }

    public void setParam3(Integer param3) {
        this.param3 = param3;
    }

    public OffsetDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}
