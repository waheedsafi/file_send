package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_batches")
@Getter
@Setter
@NoArgsConstructor
public class JobBatches {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "total_jobs", nullable = false)
    private Integer totalJobs;

    @Column(name = "pending_jobs", nullable = false)
    private Integer pendingJobs;

    @Column(name = "failed_jobs", nullable = false)
    private Integer failedJobs;

    @Column(name = "failed_job_ids", nullable = false, columnDefinition = "LONGTEXT")
    private String failedJobIds;

    @Column(name = "options", columnDefinition = "MEDIUMTEXT")
    private String options;

    @Column(name = "cancelled_at")
    private Integer cancelledAt;

    @Column(name = "created_at", nullable = false)
    private Integer createdAt;

    @Column(name = "finished_at")
    private Integer finishedAt;
}
