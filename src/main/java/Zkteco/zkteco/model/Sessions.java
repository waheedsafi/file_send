package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sessions",
    indexes = {
        @Index(name = "idx_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_sessions_last_activity", columnList = "last_activity")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Sessions {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "last_activity", nullable = false)
    private Integer lastActivity;
}
