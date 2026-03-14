package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cache_locks",
    indexes = {
        @Index(name = "idx_cache_locks_expiration", columnList = "expiration")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CacheLocks {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "expiration", nullable = false)
    private Integer expiration;
}
