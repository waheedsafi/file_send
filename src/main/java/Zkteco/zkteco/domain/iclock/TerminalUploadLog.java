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
        name = "iclock_terminaluploadlog",
        indexes = {
                @Index(name = "idx_iclock_terminaluploadlog_terminal_id", columnList = "terminal_id"),
                @Index(name = "idx_iclock_terminaluploadlog_upload_time", columnList = "upload_time")
        }
)
public class TerminalUploadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_iclock_terminaluploadlog_terminal"))
    private Terminal terminal;

    @Column(name = "event", nullable = false, length = 80)
    private String event;

    @Column(name = "content", nullable = false, length = 80)
    private String content;

    @Column(name = "upload_count", nullable = false)
    private Integer uploadCount = 1;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(Integer uploadCount) {
        this.uploadCount = uploadCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public OffsetDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}
