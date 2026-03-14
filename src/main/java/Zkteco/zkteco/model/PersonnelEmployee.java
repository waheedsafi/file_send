package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(
        name = "personnel_employee",
        indexes = {
                @Index(name = "idx_employee_status", columnList = "status"),
                @Index(name = "idx_employee_enroll_sn", columnList = "enroll_sn")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PersonnelEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime create_time;

    @Column(length = 150)
    private String create_user;

    private LocalDateTime change_time;

    @Column(length = 150)
    private String change_user;

    private Short status = 0;

    @Column(length = 20, unique = true, nullable = false)
    private String emp_code;

    @Column(length = 25)
    private String first_name;

    @Column(length = 25)
    private String last_name;

    @Column(length = 50)
    private String email;

    @Column(length = 20)
    private String enroll_sn;

    private LocalDateTime update_time;

}