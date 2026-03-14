package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_transaction",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_iclock_transaction_company_emp_punch", columnNames = {"company_code", "emp_code", "punch_time"})
    },
    indexes = {
        @Index(name = "idx_iclock_transaction_emp_id", columnList = "emp_id"),
        @Index(name = "idx_iclock_transaction_terminal_id", columnList = "terminal_id"),
        @Index(name = "idx_iclock_transaction_punch_time", columnList = "punch_time"),
        @Index(name = "idx_iclock_transaction_terminal_sn", columnList = "terminal_sn")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "emp_code", nullable = false, length = 20)
    private String empCode;

    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Column(name = "punch_state", nullable = false, length = 5)
    private String punchState;

    @Column(name = "verify_type", nullable = false)
    private Integer verifyType = 0;

    @Column(name = "work_code", length = 20)
    private String workCode;

    @Column(name = "terminal_sn", length = 50)
    private String terminalSn = "";

    @Column(name = "terminal_alias", length = 50)
    private String terminalAlias;

    @Column(name = "terminal_id")
    private Long terminalId;

    @Column(name = "area_alias", length = 30)
    private String areaAlias;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "gps_location", columnDefinition = "TEXT")
    private String gpsLocation;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "source")
    private Short source = 1;

    @Column(name = "purpose")
    private Short purpose = 1;

    @Column(name = "crc", length = 100)
    private String crc;

    @Column(name = "is_attendance")
    private Short isAttendance = 1;

    @Column(name = "reserved", length = 100)
    private String reserved;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "sync_status")
    private Short syncStatus = 0;

    @Column(name = "sync_time")
    private LocalDateTime syncTime;

    @Column(name = "is_mask")
    private Short isMask = (short) 255;

    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;
}
