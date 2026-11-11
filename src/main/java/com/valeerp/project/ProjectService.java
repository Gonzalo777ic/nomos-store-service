package com.valeerp.project;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.TimeProvider;
import com.valeerp.partner.PartnerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PartnerService partnerService;
    private final TimeProvider timeProvider;

    public ProjectService(ProjectRepository projectRepository,
                          PartnerService partnerService,
                          TimeProvider timeProvider) {
        this.projectRepository = projectRepository;
        this.partnerService = partnerService;
        this.timeProvider = timeProvider;
    }

    public Page<Project> list(ProjectStatus status, Long partnerId, Pageable pageable) {
        if (partnerId != null) {
            return projectRepository.findByPartnerId(partnerId, pageable);
        }
        if (status != null) {
            return projectRepository.findByStatus(status, pageable);
        }
        return projectRepository.findAll(pageable);
    }

    public Project require(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Proyecto no encontrado"));
    }

    @Transactional
    public Project create(ProjectCreateRequest request) {
        ensureCodeAvailable(request.code(), null);
        ensurePartner(request.partnerId());
        Project project = Project.open(
                request.code(),
                request.name(),
                request.description(),
                request.partnerId(),
                request.managerUserId(),
                resolveMoney(request.plannedBudget(), request.currency(), null),
                timeProvider.now()
        );
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, ProjectUpdateRequest request) {
        Project project = require(id);
        ensureCodeAvailable(request.code(), id);
        ensurePartner(request.partnerId());
        project.rename(
                request.code(),
                request.name(),
                request.description(),
                request.partnerId(),
                request.managerUserId()
        );
        if (request.plannedBudget() != null || (request.currency() != null && !request.currency().isBlank())) {
            project.changeBudget(resolveMoney(request.plannedBudget(), request.currency(), project));
        }
        return projectRepository.save(project);
    }

    @Transactional
    public Project activate(Long id) {
        Project project = require(id);
        project.activate(timeProvider.now());
        return projectRepository.save(project);
    }

    @Transactional
    public Project complete(Long id) {
        Project project = require(id);
        project.complete(timeProvider.now());
        return projectRepository.save(project);
    }

    @Transactional
    public Project cancel(Long id) {
        Project project = require(id);
        project.cancel(timeProvider.now());
        return projectRepository.save(project);
    }

    @Transactional
    public void retire(Long id) {
        Project project = require(id);
        project.archive(Actors.currentActor(), timeProvider.now());
        projectRepository.save(project);
    }

    public List<ProjectTask> listTasks(Long projectId) {
        return require(projectId).getTasks();
    }

    @Transactional
    public ProjectTask addTask(Long projectId, TaskCreateRequest request) {
        Project project = require(projectId);
        ProjectTask task = project.addTask(
                request.name(),
                request.description(),
                request.assigneeUserId(),
                request.plannedHours(),
                timeProvider.now()
        );
        projectRepository.save(project);
        return task;
    }

    @Transactional
    public ProjectTask updateTask(Long projectId, Long taskId, TaskUpdateRequest request) {
        Project project = require(projectId);
        project.updateTask(taskId, request.name(), request.description(), request.assigneeUserId(), request.plannedHours());
        projectRepository.save(project);
        return project.requireTask(taskId);
    }

    @Transactional
    public ProjectTask startTask(Long projectId, Long taskId) {
        Project project = require(projectId);
        project.startTask(taskId, timeProvider.now());
        projectRepository.save(project);
        return project.requireTask(taskId);
    }

    @Transactional
    public ProjectTask completeTask(Long projectId, Long taskId) {
        Project project = require(projectId);
        project.completeTask(taskId, timeProvider.now());
        projectRepository.save(project);
        return project.requireTask(taskId);
    }

    @Transactional
    public ProjectTask cancelTask(Long projectId, Long taskId) {
        Project project = require(projectId);
        project.cancelTask(taskId, timeProvider.now());
        projectRepository.save(project);
        return project.requireTask(taskId);
    }

    public List<TimesheetEntry> listTimesheets(Long projectId) {
        return require(projectId).getTimesheets();
    }

    @Transactional
    public TimesheetEntry logTimesheet(Long projectId, TimesheetCreateRequest request) {
        Project project = require(projectId);
        Money billable = resolveOptionalBillable(request.billableAmount(), request.currency(), project);
        TimesheetEntry entry = project.logTimesheet(
                request.taskId(),
                request.employeeId(),
                request.userId(),
                request.workDate(),
                request.hours(),
                billable,
                request.description(),
                timeProvider.now()
        );
        projectRepository.save(project);
        return entry;
    }

    @Transactional
    public TimesheetEntry reviseTimesheet(Long projectId, Long timesheetId, TimesheetUpdateRequest request) {
        Project project = require(projectId);
        Money billable = null;
        if (request.billableAmount() != null || (request.currency() != null && !request.currency().isBlank())) {
            billable = resolveOptionalBillable(request.billableAmount(), request.currency(), project);
        }
        project.reviseTimesheet(
                timesheetId,
                request.workDate(),
                request.hours(),
                billable,
                request.description()
        );
        projectRepository.save(project);
        return project.requireTimesheet(timesheetId);
    }

    @Transactional
    public TimesheetEntry confirmTimesheet(Long projectId, Long timesheetId) {
        Project project = require(projectId);
        project.confirmTimesheet(timesheetId, timeProvider.now());
        projectRepository.save(project);
        return project.requireTimesheet(timesheetId);
    }

    @Transactional
    public TimesheetEntry cancelTimesheet(Long projectId, Long timesheetId) {
        Project project = require(projectId);
        project.cancelTimesheet(timesheetId, timeProvider.now());
        projectRepository.save(project);
        return project.requireTimesheet(timesheetId);
    }

    public static ProjectStatus requireStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ProjectStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Estado de proyecto no soportado: " + raw);
        }
    }

    private void ensurePartner(Long partnerId) {
        if (partnerId != null) {
            partnerService.require(partnerId);
        }
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de proyecto es obligatorio");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        projectRepository.findByCode(normalized)
                .filter(existing -> excludeId == null || !excludeId.equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DomainException("Ya existe un proyecto con código " + normalized);
                });
    }

    private Money resolveMoney(BigDecimal amount, String currency, Project existing) {
        Currency resolved = currency == null || currency.isBlank()
                ? (existing == null ? ProjectPolicy.DEFAULT_CURRENCY : Currency.of(existing.getCurrency()))
                : Currency.of(currency);
        BigDecimal value = amount == null
                ? (existing == null ? BigDecimal.ZERO : existing.getPlannedBudget())
                : amount;
        return Money.of(value, resolved.code());
    }

    private Money resolveOptionalBillable(BigDecimal amount, String currency, Project project) {
        if (amount == null) {
            return null;
        }
        Currency resolved = currency == null || currency.isBlank()
                ? Currency.of(project.getCurrency())
                : Currency.of(currency);
        return Money.of(amount, resolved.code());
    }
}
