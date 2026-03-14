package Zkteco.zkteco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iclock_devicemoduleconfig")
@Getter
@Setter
@NoArgsConstructor
public class IclockDevicemoduleconfig {

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

    @Column(name = "enable_registration", nullable = false)
    private Boolean enableRegistration = false;

    @Column(name = "enable_resigned_filter", nullable = false)
    private Boolean enableResignedFilter = false;

    @Column(name = "enable_auto_add", nullable = false)
    private Boolean enableAutoAdd = true;

    @Column(name = "enable_name_upload", nullable = false)
    private Boolean enableNameUpload = true;

    @Column(name = "enable_name_download", nullable = false)
    private Boolean enableNameDownload = true;

    @Column(name = "enable_card_upload", nullable = false)
    private Boolean enableCardUpload = true;

    @Column(name = "encryption", nullable = false)
    private Boolean encryption = true;

    @Column(name = "timezone", nullable = false)
    private Short timezone = 0;

    @Column(name = "global_setup", nullable = false)
    private Boolean globalSetup = false;

    @Column(name = "heartbeat", nullable = false)
    private Integer heartbeat = 10;

    @Column(name = "transfer_mode", nullable = false)
    private Short transferMode = 1;

    @Column(name = "transfer_interval", nullable = false)
    private Integer transferInterval = 1;

    @Column(name = "transfer_time", nullable = false, length = 100)
    private String transferTime = "00:00;14:05";

    @Column(name = "sync_mode", nullable = false)
    private Short syncMode = 1;

    @Column(name = "sync_time", nullable = false, length = 100)
    private String syncTime = "00:00;12:00";

    @Column(name = "transaction_retention", nullable = false)
    private Integer transactionRetention = 9999;

    @Column(name = "command_retention", nullable = false)
    private Integer commandRetention = 90;

    @Column(name = "dev_log_retention", nullable = false)
    private Integer devLogRetention = 90;

    @Column(name = "upload_log_retention", nullable = false)
    private Integer uploadLogRetention = 90;

    @Column(name = "edit_policy", nullable = false)
    private Short editPolicy = 0;

    @Column(name = "import_policy", nullable = false)
    private Short importPolicy = 0;

    @Column(name = "mobile_policy", nullable = false)
    private Short mobilePolicy = 0;

    @Column(name = "device_policy", nullable = false)
    private Short devicePolicy = 3;

    @Column(name = "api_policy", nullable = false)
    private Short apiPolicy = 3;

    @Column(name = "visitor_policy", nullable = false)
    private Short visitorPolicy = 0;
}
