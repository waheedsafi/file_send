package Zkteco.zkteco.domain.iclock;

import Zkteco.zkteco.domain.personnel.Employee;
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
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "iclock_transaction")
public class AttendanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "emp_code", nullable = false, length = 20)
    private String empCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", foreignKey = @ForeignKey(name = "fk_iclock_transaction_employee"))
    private Employee employee;

    @Column(name = "punch_time", nullable = false)
    private OffsetDateTime punchTime;

    @Column(name = "punch_state", nullable = false, length = 5)
    private String punchState;

    @Column(name = "verify_type", nullable = false)
    private Integer verifyType = 0;

    @Column(name = "work_code", length = 20)
    private String workCode;

    @Column(name = "terminal_sn", length = 50)
    private String terminalSn;

    @Column(name = "terminal_alias", length = 50)
    private String terminalAlias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", foreignKey = @ForeignKey(name = "fk_iclock_transaction_terminal"))
    private Terminal terminal;

    @Column(name = "area_alias", length = 30)
    private String areaAlias;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "gps_location")
    private String gpsLocation;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "source")
    private Short source;

    @Column(name = "purpose")
    private Short purpose;

    @Column(name = "crc", length = 100)
    private String crc;

    @Column(name = "is_attendance")
    private Short isAttendance;

    @Column(name = "reserved", length = 100)
    private String reserved;

    @Column(name = "upload_time")
    private OffsetDateTime uploadTime;

    @Column(name = "sync_status")
    private Short syncStatus;

    @Column(name = "sync_time")
    private OffsetDateTime syncTime;

    @Column(name = "is_mask")
    private Short isMask;

    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public OffsetDateTime getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(OffsetDateTime punchTime) {
        this.punchTime = punchTime;
    }

    public String getPunchState() {
        return punchState;
    }

    public void setPunchState(String punchState) {
        this.punchState = punchState;
    }

    public Integer getVerifyType() {
        return verifyType;
    }

    public void setVerifyType(Integer verifyType) {
        this.verifyType = verifyType;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public String getTerminalSn() {
        return terminalSn;
    }

    public void setTerminalSn(String terminalSn) {
        this.terminalSn = terminalSn;
    }

    public String getTerminalAlias() {
        return terminalAlias;
    }

    public void setTerminalAlias(String terminalAlias) {
        this.terminalAlias = terminalAlias;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public String getAreaAlias() {
        return areaAlias;
    }

    public void setAreaAlias(String areaAlias) {
        this.areaAlias = areaAlias;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Short getSource() {
        return source;
    }

    public void setSource(Short source) {
        this.source = source;
    }

    public Short getPurpose() {
        return purpose;
    }

    public void setPurpose(Short purpose) {
        this.purpose = purpose;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public Short getIsAttendance() {
        return isAttendance;
    }

    public void setIsAttendance(Short isAttendance) {
        this.isAttendance = isAttendance;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public OffsetDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(OffsetDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Short getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Short syncStatus) {
        this.syncStatus = syncStatus;
    }

    public OffsetDateTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(OffsetDateTime syncTime) {
        this.syncTime = syncTime;
    }

    public Short getIsMask() {
        return isMask;
    }

    public void setIsMask(Short isMask) {
        this.isMask = isMask;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
