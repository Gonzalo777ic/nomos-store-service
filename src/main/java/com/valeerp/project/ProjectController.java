package com.valeerp.project;

import com.valeerp.kernel.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long partnerId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        ProjectStatus projectStatus = ProjectService.requireStatus(status);
        return PageResponse.from(projectService.list(projectStatus, partnerId, pageable).map(ProjectResponse::from));
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        return ProjectResponse.from(projectService.require(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody ProjectCreateRequest request) {
        return ProjectResponse.from(projectService.create(request));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectUpdateRequest request) {
        return ProjectResponse.from(projectService.update(id, request));
    }

    @PostMapping("/{id}/activate")
    public ProjectResponse activate(@PathVariable Long id) {
        return ProjectResponse.from(projectService.activate(id));
    }

    @PostMapping("/{id}/complete")
    public ProjectResponse complete(@PathVariable Long id) {
        return ProjectResponse.from(projectService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    public ProjectResponse cancel(@PathVariable Long id) {
        return ProjectResponse.from(projectService.cancel(id));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        projectService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public List<TaskResponse> tasks(@PathVariable Long id) {
        return projectService.listTasks(id).stream().map(TaskResponse::from).toList();
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse addTask(@PathVariable Long id, @RequestBody TaskCreateRequest request) {
        return TaskResponse.from(projectService.addTask(id, request));
    }

    @PutMapping("/{id}/tasks/{taskId}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @PathVariable Long taskId,
                                   @RequestBody TaskUpdateRequest request) {
        return TaskResponse.from(projectService.updateTask(id, taskId, request));
    }

    @PostMapping("/{id}/tasks/{taskId}/start")
    public TaskResponse startTask(@PathVariable Long id, @PathVariable Long taskId) {
        return TaskResponse.from(projectService.startTask(id, taskId));
    }

    @PostMapping("/{id}/tasks/{taskId}/complete")
    public TaskResponse completeTask(@PathVariable Long id, @PathVariable Long taskId) {
        return TaskResponse.from(projectService.completeTask(id, taskId));
    }

    @PostMapping("/{id}/tasks/{taskId}/cancel")
    public TaskResponse cancelTask(@PathVariable Long id, @PathVariable Long taskId) {
        return TaskResponse.from(projectService.cancelTask(id, taskId));
    }

    @GetMapping("/{id}/timesheets")
    public List<TimesheetResponse> timesheets(@PathVariable Long id) {
        return projectService.listTimesheets(id).stream().map(TimesheetResponse::from).toList();
    }

    @PostMapping("/{id}/timesheets")
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetResponse logTimesheet(@PathVariable Long id, @RequestBody TimesheetCreateRequest request) {
        return TimesheetResponse.from(projectService.logTimesheet(id, request));
    }

    @PutMapping("/{id}/timesheets/{timesheetId}")
    public TimesheetResponse reviseTimesheet(@PathVariable Long id,
                                             @PathVariable Long timesheetId,
                                             @RequestBody TimesheetUpdateRequest request) {
        return TimesheetResponse.from(projectService.reviseTimesheet(id, timesheetId, request));
    }

    @PostMapping("/{id}/timesheets/{timesheetId}/confirm")
    public TimesheetResponse confirmTimesheet(@PathVariable Long id, @PathVariable Long timesheetId) {
        return TimesheetResponse.from(projectService.confirmTimesheet(id, timesheetId));
    }

    @PostMapping("/{id}/timesheets/{timesheetId}/cancel")
    public TimesheetResponse cancelTimesheet(@PathVariable Long id, @PathVariable Long timesheetId) {
        return TimesheetResponse.from(projectService.cancelTimesheet(id, timesheetId));
    }

    @GetMapping("/statuses")
    public List<CatalogItem> statuses() {
        return Arrays.stream(ProjectStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    @GetMapping("/task-statuses")
    public List<CatalogItem> taskStatuses() {
        return Arrays.stream(TaskStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    @GetMapping("/timesheet-statuses")
    public List<CatalogItem> timesheetStatuses() {
        return Arrays.stream(TimesheetStatus.values())
                .map(status -> new CatalogItem(status.name(), status.name()))
                .toList();
    }

    public record CatalogItem(String key, String label) {
    }
}
