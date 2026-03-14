package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_terminaluploadlog",
    indexes = {
        @Index(name = "idx_iclock_terminaluploadlog_terminal_id", columnList = "terminal_id"),
        @Index(name = "idx_iclock_terminaluploadlog_upload_time", columnList = "upload_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminaluploadlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "event", nullable = false, length = 80)
    private String event;

    @Column(name = "content", nullable = false, length = 80)
    private String content;

    @Column(name = "upload_count", nullable = false)
    private Integer uploadCount = 1;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
}
