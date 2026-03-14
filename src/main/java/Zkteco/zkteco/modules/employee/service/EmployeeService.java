package Zkteco.zkteco.modules.employee.service;

import Zkteco.zkteco.domain.personnel.Area;
import Zkteco.zkteco.domain.personnel.Company;
import Zkteco.zkteco.domain.personnel.Department;
import Zkteco.zkteco.domain.personnel.Employee;
import Zkteco.zkteco.domain.personnel.Position;
import Zkteco.zkteco.repository.personnel.AreaRepository;
import Zkteco.zkteco.repository.personnel.CompanyRepository;
import Zkteco.zkteco.repository.personnel.DepartmentRepository;
import Zkteco.zkteco.repository.personnel.EmployeeRepository;
import Zkteco.zkteco.repository.personnel.PositionRepository;
import Zkteco.zkteco.modules.employee.dto.EmployeeResponse;
import Zkteco.zkteco.modules.employee.dto.EmployeeUpsertRequest;
import Zkteco.zkteco.web.error.BadRequestException;
import Zkteco.zkteco.web.error.NotFoundException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private static final long DEFAULT_COMPANY_ID = 1L;
    private static final long DEFAULT_DEPARTMENT_ID = 1L;
    private static final long DEFAULT_AREA_ID = 1L;

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AreaRepository areaRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            AreaRepository areaRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> list(Long companyId, String q) {
        Long resolvedCompanyId = companyId != null ? companyId : DEFAULT_COMPANY_ID;
        List<Employee> employees;
        if (q == null || q.isBlank()) {
            employees = employeeRepository.findByCompanyIdOrderByIdAsc(resolvedCompanyId);
        } else {
            employees = employeeRepository.findByCompanyIdAndEmpCodeContainingIgnoreCaseOrderByIdAsc(resolvedCompanyId, q.trim());
        }
        return employees.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        Employee employee = findEmployee(id);
        return toResponse(employee);
    }

    public EmployeeResponse create(EmployeeUpsertRequest request) {
        Long companyId = request.getCompanyId() != null ? request.getCompanyId() : DEFAULT_COMPANY_ID;
        String empCode = sanitizeCode(request.getEmpCode());
        if (employeeRepository.existsByCompanyIdAndEmpCode(companyId, empCode)) {
            throw new BadRequestException("Employee code already exists in this company: " + empCode);
        }

        Employee employee = new Employee();
        employee.setCompany(findCompany(companyId));
        applyFields(employee, request, true);
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeUpsertRequest request) {
        Employee employee = findEmployee(id);
        String empCode = sanitizeCode(request.getEmpCode());
        if (!employee.getEmpCode().equals(empCode)
                && employeeRepository.existsByCompanyIdAndEmpCode(employee.getCompany().getId(), empCode)) {
            throw new BadRequestException("Employee code already exists in this company: " + empCode);
        }

        applyFields(employee, request, false);
        return toResponse(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = findEmployee(id);
        employeeRepository.delete(employee);
    }

    private void applyFields(Employee employee, EmployeeUpsertRequest request, boolean isCreate) {
        String empCode = sanitizeCode(request.getEmpCode());
        employee.setEmpCode(empCode);
        employee.setEmpCodeDigit(parseEmpCodeDigit(empCode));
        employee.setFirstName(trimToNull(request.getFirstName()));
        employee.setLastName(trimToNull(request.getLastName()));

        Long companyId = request.getCompanyId() != null ? request.getCompanyId() : employee.getCompany().getId();
        employee.setCompany(findCompany(companyId));

        Long departmentId = request.getDepartmentId() != null ? request.getDepartmentId() : DEFAULT_DEPARTMENT_ID;
        employee.setDepartment(findDepartment(departmentId));

        if (request.getPositionId() != null) {
            employee.setPosition(findPosition(request.getPositionId()));
        } else if (isCreate) {
            employee.setPosition(null);
        }

        if (request.getSuperiorId() != null) {
            if (employee.getId() != null && Objects.equals(employee.getId(), request.getSuperiorId())) {
                throw new BadRequestException("Employee cannot be superior of itself");
            }
            employee.setSuperior(findEmployee(request.getSuperiorId()));
        } else if (isCreate) {
            employee.setSuperior(null);
        }

        Set<Area> areas = resolveAreas(request.getAreaIds());
        employee.setAreas(areas);

        employee.setCardNo(normalizeCardNo(request.getCardNo()));
        employee.setDevicePassword(trimToNull(request.getDevicePassword()));
        employee.setDevPrivilege(request.getDevPrivilege() != null ? request.getDevPrivilege() : 0);
        employee.setVerifyMode(request.getVerifyMode() != null ? request.getVerifyMode() : 0);
        employee.setEnablePayroll(request.getEnablePayroll() == null || request.getEnablePayroll());
        employee.setEmail(trimToNull(request.getEmail()));
        employee.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
        employee.setUpdateTime(OffsetDateTime.now());

        if (employee.getAppStatus() == null) {
            employee.setAppStatus((short) 0);
        }
        if (employee.getAppRole() == null) {
            employee.setAppRole((short) 1);
        }
    }

    private Set<Area> resolveAreas(List<Long> areaIds) {
        List<Long> ids = new ArrayList<>();
        if (areaIds != null && !areaIds.isEmpty()) {
            ids.addAll(areaIds);
        } else {
            ids.add(DEFAULT_AREA_ID);
        }
        List<Area> areas = areaRepository.findByIdIn(ids);
        if (areas.isEmpty()) {
            throw new BadRequestException("No valid area IDs were provided");
        }
        return new LinkedHashSet<>(areas);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found: " + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }

    private Position findPosition(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Position not found: " + id));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmpCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getCompany().getId(),
                employee.getCompany().getCompanyCode(),
                employee.getCompany().getCompanyName(),
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getDepartment() != null ? employee.getDepartment().getDeptName() : null,
                employee.getPosition() != null ? employee.getPosition().getId() : null,
                employee.getPosition() != null ? employee.getPosition().getPositionName() : null,
                employee.getSuperior() != null ? employee.getSuperior().getId() : null,
                employee.getAreas().stream().map(Area::getId).collect(Collectors.toList()),
                employee.getCardNo(),
                employee.getDevicePassword(),
                employee.getDevPrivilege(),
                employee.getVerifyMode(),
                employee.isEnablePayroll(),
                employee.getEmail(),
                employee.getHireDate(),
                employee.getUpdateTime(),
                employee.getStatus()
        );
    }

    private String sanitizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("empCode is required");
        }
        String trimmed = code.trim();
        if (trimmed.contains(" ")) {
            throw new BadRequestException("empCode cannot contain whitespace");
        }
        return trimmed;
    }

    private Long parseEmpCodeDigit(String code) {
        if (code == null || code.isBlank() || !code.chars().allMatch(Character::isDigit)) {
            return null;
        }
        if (code.length() > 18) {
            return null;
        }
        try {
            return Long.parseLong(code);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeCardNo(String cardNo) {
        String value = trimToNull(cardNo);
        if (value == null) {
            return null;
        }
        String stripped = value.replaceFirst("^0+(?!$)", "");
        return stripped.isBlank() ? null : stripped;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
