package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;




@Entity
@Table(name = "iclock_biophoto",
        indexes = {
                @Index(columnList="employee_id"),
                @Index(columnList="approval_state"),
                @Index(columnList="register_time")
        })
@Getter @Setter @NoArgsConstructor
public class IclockBiophoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employee_id;

    private String first_name = "";
    private String last_name;
    private String email;
    private String enroll_sn;

    private String register_photo = "";
    private LocalDateTime register_time;

    private String approval_photo;
    private Short approval_state = 0;
    private LocalDateTime approval_time;

    private String remark;
}