package Zkteco.zkteco.domain.iclock;

import Zkteco.zkteco.domain.common.AuditableStatusEntity;
import Zkteco.zkteco.domain.personnel.Area;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "iclock_terminal")
public class Terminal extends AuditableStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sn", nullable = false, unique = true, length = 50)
    private String sn;

    @Column(name = "alias", nullable = false, length = 50)
    private String alias;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "terminal_tz")
    private Short terminalTz;

    @Column(name = "heartbeat")
    private Integer heartbeat;

    @Column(name = "transfer_mode")
    private Short transferMode;

    @Column(name = "transfer_interval")
    private Integer transferInterval;

    @Column(name = "transfer_time", length = 100)
    private String transferTime;

    @Column(name = "fw_ver", length = 100)
    private String fwVer;

    @Column(name = "push_protocol", length = 30)
    private String pushProtocol;

    @Column(name = "push_ver", length = 30)
    private String pushVer;

    @Column(name = "language")
    private Integer language;

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
    private Integer fvCount;

    @Column(name = "fv_alg_ver", length = 10)
    private String fvAlgVer;

    @Column(name = "palm_count")
    private Integer palmCount;

    @Column(name = "palm_alg_ver", length = 10)
    private String palmAlgVer;

    @Column(name = "lock_func")
    private Short lockFunc;

    @Column(name = "log_stamp", length = 30)
    private String logStamp;

    @Column(name = "op_log_stamp", length = 30)
    private String opLogStamp;

    @Column(name = "capture_stamp", length = 30)
    private String captureStamp;

    @Column(name = "real_ip", length = 45)
    private String realIp;

    @Column(name = "state", nullable = false)
    private Integer state = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", foreignKey = @ForeignKey(name = "fk_iclock_terminal_area"))
    private Area area;

    @Column(name = "product_type")
    private Short productType;

    @Column(name = "is_attendance")
    private Short isAttendance;

    @Column(name = "is_registration")
    private Short isRegistration;

    @Column(name = "purpose")
    private Short purpose;

    @Column(name = "controller_type")
    private Short controllerType;

    @Column(name = "authentication")
    private Short authentication;

    @Column(name = "style", length = 20)
    private String style;

    @Column(name = "upload_flag", length = 10)
    private String uploadFlag;

    @Column(name = "is_tft", nullable = false)
    private boolean tft;

    @Column(name = "last_activity")
    private OffsetDateTime lastActivity;

    @Column(name = "upload_time")
    private OffsetDateTime uploadTime;

    @Column(name = "push_time")
    private OffsetDateTime pushTime;

    @Column(name = "is_access")
    private Short isAccess;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Short getTerminalTz() {
        return terminalTz;
    }

    public void setTerminalTz(Short terminalTz) {
        this.terminalTz = terminalTz;
    }

    public Integer getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    public Short getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(Short transferMode) {
        this.transferMode = transferMode;
    }

    public Integer getTransferInterval() {
        return transferInterval;
    }

    public void setTransferInterval(Integer transferInterval) {
        this.transferInterval = transferInterval;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public String getFwVer() {
        return fwVer;
    }

    public void setFwVer(String fwVer) {
        this.fwVer = fwVer;
    }

    public String getPushProtocol() {
        return pushProtocol;
    }

    public void setPushProtocol(String pushProtocol) {
        this.pushProtocol = pushProtocol;
    }

    public String getPushVer() {
        return pushVer;
    }

    public void setPushVer(String pushVer) {
        this.pushVer = pushVer;
    }

    public Integer getLanguage() {
        return language;
    }

    public void setLanguage(Integer language) {
        this.language = language;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOemVendor() {
        return oemVendor;
    }

    public void setOemVendor(String oemVendor) {
        this.oemVendor = oemVendor;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Integer getFpCount() {
        return fpCount;
    }

    public void setFpCount(Integer fpCount) {
        this.fpCount = fpCount;
    }

    public String getFpAlgVer() {
        return fpAlgVer;
    }

    public void setFpAlgVer(String fpAlgVer) {
        this.fpAlgVer = fpAlgVer;
    }

    public Integer getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(Integer faceCount) {
        this.faceCount = faceCount;
    }

    public String getFaceAlgVer() {
        return faceAlgVer;
    }

    public void setFaceAlgVer(String faceAlgVer) {
        this.faceAlgVer = faceAlgVer;
    }

    public Integer getFvCount() {
        return fvCount;
    }

    public void setFvCount(Integer fvCount) {
        this.fvCount = fvCount;
    }

    public String getFvAlgVer() {
        return fvAlgVer;
    }

    public void setFvAlgVer(String fvAlgVer) {
        this.fvAlgVer = fvAlgVer;
    }

    public Integer getPalmCount() {
        return palmCount;
    }

    public void setPalmCount(Integer palmCount) {
        this.palmCount = palmCount;
    }

    public String getPalmAlgVer() {
        return palmAlgVer;
    }

    public void setPalmAlgVer(String palmAlgVer) {
        this.palmAlgVer = palmAlgVer;
    }

    public Short getLockFunc() {
        return lockFunc;
    }

    public void setLockFunc(Short lockFunc) {
        this.lockFunc = lockFunc;
    }

    public String getLogStamp() {
        return logStamp;
    }

    public void setLogStamp(String logStamp) {
        this.logStamp = logStamp;
    }

    public String getOpLogStamp() {
        return opLogStamp;
    }

    public void setOpLogStamp(String opLogStamp) {
        this.opLogStamp = opLogStamp;
    }

    public String getCaptureStamp() {
        return captureStamp;
    }

    public void setCaptureStamp(String captureStamp) {
        this.captureStamp = captureStamp;
    }

    public String getRealIp() {
        return realIp;
    }

    public void setRealIp(String realIp) {
        this.realIp = realIp;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Short getProductType() {
        return productType;
    }

    public void setProductType(Short productType) {
        this.productType = productType;
    }

    public Short getIsAttendance() {
        return isAttendance;
    }

    public void setIsAttendance(Short isAttendance) {
        this.isAttendance = isAttendance;
    }

    public Short getIsRegistration() {
        return isRegistration;
    }

    public void setIsRegistration(Short isRegistration) {
        this.isRegistration = isRegistration;
    }

    public Short getPurpose() {
        return purpose;
    }

    public void setPurpose(Short purpose) {
        this.purpose = purpose;
    }

    public Short getControllerType() {
        return controllerType;
    }

    public void setControllerType(Short controllerType) {
        this.controllerType = controllerType;
    }

    public Short getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Short authentication) {
        this.authentication = authentication;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUploadFlag() {
        return uploadFlag;
    }

    public void setUploadFlag(String uploadFlag) {
        this.uploadFlag = uploadFlag;
    }

    public boolean isTft() {
        return tft;
    }

    public void setTft(boolean tft) {
        this.tft = tft;
    }

    public OffsetDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public OffsetDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public OffsetDateTime getPushTime() {
        return pushTime;
    }

    public void setPushTime(OffsetDateTime pushTime) {
        this.pushTime = pushTime;
    }

    public Short getIsAccess() {
        return isAccess;
    }

    public void setIsAccess(Short isAccess) {
        this.isAccess = isAccess;
    }
}
