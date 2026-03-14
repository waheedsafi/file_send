package Zkteco.zkteco.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "iclock_transactionproofcmd",
        indexes = {
                @Index(columnList="terminal_id"),
                @Index(columnList="action_time")
        })
@Getter
@Setter
@NoArgsConstructor
public class IclockTransactionproofcmd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long terminal_id;
    private LocalDateTime action_time;
    private LocalDateTime start_time;
    private LocalDateTime end_time;

    private Integer terminal_count;
    private Integer server_count;

    private Short flag = 0;
    private Integer reserved_init = 0;
    private Double reserved_float = 0.0;
    private String reserved_char = "0";

}