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
@Table(name = "personnel_position")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_code", nullable = false, length = 50)
    private String positionCode;

    @Column(name = "position_name", nullable = false, length = 100)
    private String positionName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_position_id", foreignKey = @ForeignKey(name = "fk_personnel_position_parent"))
    private Position parentPosition;

    @Column(name = "is_default", nullable = false)
    private boolean defaultPosition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personnel_position_company"))
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public Position getParentPosition() {
        return parentPosition;
    }

    public void setParentPosition(Position parentPosition) {
        this.parentPosition = parentPosition;
    }

    public boolean isDefaultPosition() {
        return defaultPosition;
    }

    public void setDefaultPosition(boolean defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
