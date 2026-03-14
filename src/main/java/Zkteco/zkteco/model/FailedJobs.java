package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "failed_jobs")
@Getter
@Setter
@NoArgsConstructor
public class FailedJobs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "connection", nullable = false, columnDefinition = "TEXT")
    private String connection;

    @Column(name = "queue", nullable = false, columnDefinition = "TEXT")
    private String queue;

    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "exception", nullable = false, columnDefinition = "LONGTEXT")
    private String exception;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;
}
