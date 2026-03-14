package Zkteco.zkteco.domain.iclock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "iclock_biodata",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_iclock_biodata_tpl",
                columnNames = {"employee_id", "bio_no", "bio_index", "bio_type", "bio_format", "major_ver"}
        )
)
public class BioDataTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Lob
    @Column(name = "bio_tmp", nullable = false)
    private String bioTmp;

    @Column(name = "bio_no")
    private Integer bioNo = 0;

    @Column(name = "bio_index")
    private Integer bioIndex = 0;

    @Column(name = "bio_type", nullable = false)
    private Integer bioType;

    @Column(name = "major_ver", nullable = false, length = 10)
    private String majorVer;

    @Column(name = "minor_ver", length = 10)
    private String minorVer;

    @Column(name = "bio_format")
    private Integer bioFormat = 0;

    @Column(name = "valid")
    private Integer valid = 1;

    @Column(name = "duress")
    private Integer duress = 0;

    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    @Column(name = "sn", length = 50)
    private String sn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getBioTmp() {
        return bioTmp;
    }

    public void setBioTmp(String bioTmp) {
        this.bioTmp = bioTmp;
    }

    public Integer getBioNo() {
        return bioNo;
    }

    public void setBioNo(Integer bioNo) {
        this.bioNo = bioNo;
    }

    public Integer getBioIndex() {
        return bioIndex;
    }

    public void setBioIndex(Integer bioIndex) {
        this.bioIndex = bioIndex;
    }

    public Integer getBioType() {
        return bioType;
    }

    public void setBioType(Integer bioType) {
        this.bioType = bioType;
    }

    public String getMajorVer() {
        return majorVer;
    }

    public void setMajorVer(String majorVer) {
        this.majorVer = majorVer;
    }

    public String getMinorVer() {
        return minorVer;
    }

    public void setMinorVer(String minorVer) {
        this.minorVer = minorVer;
    }

    public Integer getBioFormat() {
        return bioFormat;
    }

    public void setBioFormat(Integer bioFormat) {
        this.bioFormat = bioFormat;
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }

    public Integer getDuress() {
        return duress;
    }

    public void setDuress(Integer duress) {
        this.duress = duress;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(OffsetDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
