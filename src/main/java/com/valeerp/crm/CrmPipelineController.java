package com.valeerp.crm;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/crm/pipelines")
public class CrmPipelineController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CrmPipelineService pipelineService;

    public CrmPipelineController(CrmPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping
    public PageResponse<PipelineResponse> list(@PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return PageResponse.from(pipelineService.list(pageable).map(PipelineResponse::from));
    }

    @GetMapping("/{id}")
    public PipelineResponse get(@PathVariable Long id) {
        return PipelineResponse.from(pipelineService.require(id));
    }

    @GetMapping("/{id}/board")
    public PipelineBoardResponse board(@PathVariable Long id) {
        return pipelineService.board(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PipelineResponse create(@RequestBody PipelineCreateRequest request) {
        return PipelineResponse.from(pipelineService.create(request));
    }

    @PutMapping("/{id}")
    public PipelineResponse update(@PathVariable Long id, @RequestBody PipelineUpdateRequest request) {
        return PipelineResponse.from(pipelineService.update(id, request));
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<Void> retire(@PathVariable Long id) {
        pipelineService.retire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stages")
    public List<StageResponse> stages(@PathVariable Long id) {
        return pipelineService.require(id).activeStages().stream().map(StageResponse::from).toList();
    }

    @PostMapping("/{id}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public StageResponse addStage(@PathVariable Long id, @RequestBody StageCreateRequest request) {
        return StageResponse.from(pipelineService.addStage(id, request));
    }

    @PutMapping("/{id}/stages/{stageId}")
    public StageResponse updateStage(@PathVariable Long id,
                                     @PathVariable Long stageId,
                                     @RequestBody StageUpdateRequest request) {
        return StageResponse.from(pipelineService.updateStage(id, stageId, request));
    }

    @PostMapping("/{id}/stages/{stageId}/retire")
    public ResponseEntity<Void> retireStage(@PathVariable Long id, @PathVariable Long stageId) {
        pipelineService.retireStage(id, stageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stage-outcomes")
    public List<CatalogItem> stageOutcomes() {
        return Arrays.stream(StageOutcome.values())
                .map(outcome -> new CatalogItem(outcome.name(), outcome.name()))
                .toList();
    }

    public record CatalogItem(String key, String label) {
    }
}
