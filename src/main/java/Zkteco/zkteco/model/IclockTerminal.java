package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
@Table(name = "iclock_terminal",
    indexes = {
        @Index(name = "idx_iclock_terminal_area_id", columnList = "area_id"),
        @Index(name = "idx_iclock_terminal_last_activity", columnList = "last_activity"),
        @Index(name = "idx_iclock_terminal_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class IclockTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "create_user", length = 150)
    private String createUser;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(name = "change_user", length = 150)
    private String changeUser;

    @Column(name = "status", nullable = false)
    private Short status = 0;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "terminal_tz", nullable = false)
    private Short terminalTz = 0;

    @Column(name = "heartbeat", nullable = false)
    private Integer heartbeat = 10;

    @Column(name = "transfer_mode", nullable = false)
    private Short transferMode = 1;

    @Column(name = "transfer_interval", nullable = false)
    private Integer transferInterval = 1;

    @Column(name = "transfer_time", nullable = false, length = 100)
    private String transferTime = "00:00;14:05";

    @Column(name = "fw_ver", length = 100)
    private String fwVer;

    @Column(name = "push_protocol", nullable = false, length = 30)
    private String pushProtocol = "";

    @Column(name = "push_ver", length = 30)
    private String pushVer;

    @Column(name = "language")
    private Integer language = 84;

    @Column(name = "terminal_name", length = 30)
    private String terminalName;

    @Column(name = "platform", length = 30)
    private String platform;

    @Column(name = "oem_vendor", length = 50)
    private String oemVendor;

    @Column(name = "user_count")
    private Integer userCount;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "fp_count")
    private Integer fpCount;

    @Column(name = "fp_alg_ver", length = 10)
    private String fpAlgVer;

    @Column(name = "face_count")
    private Integer faceCount;

    @Column(name = "face_alg_ver", length = 10)
    private String faceAlgVer;

    @Column(name = "fv_count")
    private Integer fvCount = 0;

    @Column(name = "fv_alg_ver", length = 10)
    private String fvAlgVer;

    @Column(name = "palm_count")
    private Integer palmCount;

    @Column(name = "palm_alg_ver", length = 10)
    private String palmAlgVer;

    @Column(name = "lock_func", nullable = false)
    private Short lockFunc = 0;

    @Column(name = "log_stamp", length = 30)
    private String logStamp;

    @Column(name = "op_log_stamp", length = 30)
    private String opLogStamp;

    @Column(name = "capture_stamp", length = 30)
    private String captureStamp;

    @Column(name = "sn", nullable = false, unique = true, length = 50)
    private String sn;

    @Column(name = "alias", nullable = false, length = 50)
    private String alias;

    @Column(name = "real_ip", length = 45)
    private String realIp;

    @Column(name = "state", nullable = false)
    private Integer state = 1;

    @Column(name = "area_id")
    private Long areaId;

    @Column(name = "product_type")
    private Short productType = 9;

    @Column(name = "is_attendance", nullable = false)
    private Short isAttendance = 1;

    @Column(name = "is_registration", nullable = false)
    private Short isRegistration = 0;

    @Column(name = "purpose")
    private Short purpose = 1;

    @Column(name = "controller_type")
    private Short controllerType = 0;

    @Column(name = "authentication", nullable = false)
    private Short authentication = 1;

    @Column(name = "style", length = 20)
    private String style;

    @Column(name = "upload_flag", length = 10)
    private String uploadFlag = "1111100000";

    @Column(name = "is_tft", nullable = false)
    private Boolean isTft = false;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "push_time")
    private LocalDateTime pushTime;

    @Column(name = "is_access", nullable = false)
    private Short isAccess = 0;
}
