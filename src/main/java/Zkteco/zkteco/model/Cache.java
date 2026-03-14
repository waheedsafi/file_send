package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cache",
    indexes = {
        @Index(name = "idx_cache_expiration", columnList = "expiration")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Cache {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String value;

    @Column(name = "expiration", nullable = false)
    private Integer expiration;
}
