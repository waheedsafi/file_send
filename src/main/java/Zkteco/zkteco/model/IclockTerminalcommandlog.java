package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_terminalcommandlog",
    indexes = {
        @Index(name = "idx_iclock_terminalcommandlog_terminal_id", columnList = "terminal_id"),
        @Index(name = "idx_iclock_terminalcommandlog_commit_time", columnList = "commit_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminalcommandlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "commit_time", nullable = false)
    private LocalDateTime commitTime;

    @Column(name = "transfer_time")
    private LocalDateTime transferTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "return_value")
    private Integer returnValue;

    @Column(name = "package")
    private Integer packageNum;
}
