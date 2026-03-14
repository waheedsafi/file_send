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
        name = "iclock_errorcommandlog",
        indexes = {
                @Index(name = "idx_iclock_errorcommandlog_terminal_id", columnList = "terminal_id"),
                @Index(name = "idx_iclock_errorcommandlog_upload_time", columnList = "upload_time")
        }
)
public class ErrorCommandLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_iclock_errorcommandlog_terminal"))
    private Terminal terminal;

    @Column(name = "error_code", length = 16)
    private String errorCode;

    @Column(name = "error_msg", length = 50)
    private String errorMsg;

    @Column(name = "data_origin")
    private String dataOrigin;

    @Column(name = "cmd", length = 50)
    private String cmd;

    @Column(name = "additional")
    private String additional;

    @Column(name = "upload_time", nullable = false)
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getDataOrigin() {
        return dataOrigin;
    }

    public void setDataOrigin(String dataOrigin) {
        this.dataOrigin = dataOrigin;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    public OffsetDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}
