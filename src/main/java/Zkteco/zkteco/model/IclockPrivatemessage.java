package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_privatemessage",
    indexes = {
        @Index(name = "idx_iclock_privatemessage_employee_id", columnList = "employee_id"),
        @Index(name = "idx_iclock_privatemessage_message_id", columnList = "message_id"),
        @Index(name = "idx_iclock_privatemessage_last_send", columnList = "last_send")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockPrivatemessage {

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

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "last_send")
    private LocalDateTime lastSend;
}
