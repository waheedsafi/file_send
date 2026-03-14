package Zkteco.zkteco.modules.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class EmployeeUpsertRequest {

    @NotBlank
    @Size(max = 20)
    private String empCode;

    @Size(max = 25)
    private String firstName;

    @Size(max = 25)
    private String lastName;

    private Long companyId;

    private Long departmentId;

    private Long positionId;

    private Long superiorId;

    private List<Long> areaIds;

    @Size(max = 20)
    private String cardNo;

    @Size(max = 20)
    private String devicePassword;

    private Integer devPrivilege;

    private Integer verifyMode;

    private Boolean enablePayroll;

    @Email
    @Size(max = 50)
    private String email;

    private LocalDate hireDate;

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getSuperiorId() {
        return superiorId;
    }

    public void setSuperiorId(Long superiorId) {
        this.superiorId = superiorId;
    }

    public List<Long> getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(List<Long> areaIds) {
        this.areaIds = areaIds;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getDevicePassword() {
        return devicePassword;
    }

    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }

    public Integer getDevPrivilege() {
        return devPrivilege;
    }

    public void setDevPrivilege(Integer devPrivilege) {
        this.devPrivilege = devPrivilege;
    }

    public Integer getVerifyMode() {
        return verifyMode;
    }

    public void setVerifyMode(Integer verifyMode) {
        this.verifyMode = verifyMode;
    }

    public Boolean getEnablePayroll() {
        return enablePayroll;
    }

    public void setEnablePayroll(Boolean enablePayroll) {
        this.enablePayroll = enablePayroll;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
