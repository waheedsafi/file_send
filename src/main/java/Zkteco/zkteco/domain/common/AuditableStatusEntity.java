package Zkteco.zkteco.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class AuditableStatusEntity {

    @Column(name = "create_time", nullable = false)
    private OffsetDateTime createTime;

    @Column(name = "create_user", length = 150)
    private String createUser;

    @Column(name = "change_time", nullable = false)
    private OffsetDateTime changeTime;

    @Column(name = "change_user", length = 150)
    private String changeUser;

    @Column(name = "status", nullable = false)
    private short status = 0;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        changeTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        changeTime = OffsetDateTime.now();
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(OffsetDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public OffsetDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(OffsetDateTime changeTime) {
        this.changeTime = changeTime;
    }

    public String getChangeUser() {
        return changeUser;
    }

    public void setChangeUser(String changeUser) {
        this.changeUser = changeUser;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }
}
