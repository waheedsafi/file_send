package Zkteco.zkteco.domain.personnel;

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

@Entity
@Table(name = "personnel_department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dept_code", nullable = false, length = 50)
    private String deptCode;

    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dept_id", foreignKey = @ForeignKey(name = "fk_personnel_department_parent"))
    private Department parentDept;

    @Column(name = "is_default", nullable = false)
    private boolean defaultDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_manager_id", foreignKey = @ForeignKey(name = "fk_personnel_department_manager"))
    private Employee deptManager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personnel_department_company"))
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Department getParentDept() {
        return parentDept;
    }

    public void setParentDept(Department parentDept) {
        this.parentDept = parentDept;
    }

    public boolean isDefaultDepartment() {
        return defaultDepartment;
    }

    public void setDefaultDepartment(boolean defaultDepartment) {
        this.defaultDepartment = defaultDepartment;
    }

    public Employee getDeptManager() {
        return deptManager;
    }

    public void setDeptManager(Employee deptManager) {
        this.deptManager = deptManager;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
