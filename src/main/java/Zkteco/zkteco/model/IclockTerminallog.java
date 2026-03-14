package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_terminallog",
    indexes = {
        @Index(name = "idx_iclock_terminallog_terminal_id", columnList = "terminal_id"),
        @Index(name = "idx_iclock_terminallog_action_time", columnList = "action_time"),
        @Index(name = "idx_iclock_terminallog_upload_time", columnList = "upload_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminallog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", nullable = false)
    private Long terminalId;

    @Column(name = "terminal_tz")
    private Short terminalTz;

    @Column(name = "admin", length = 50)
    private String admin;

    @Column(name = "action_name")
    private Short actionName;

    @Column(name = "action_time")
    private LocalDateTime actionTime;

    @Column(name = "object", length = 50)
    private String object;

    @Column(name = "param1")
    private Integer param1;

    @Column(name = "param2")
    private Integer param2;

    @Column(name = "param3")
    private Integer param3;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
}
