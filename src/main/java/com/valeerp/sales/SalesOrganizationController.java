package com.valeerp.sales;

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

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesOrganizationController {

    private final SalesOrganizationService organizationService;

    public SalesOrganizationController(SalesOrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/teams")
    public List<SalesTeamResponse> listTeams() {
        return organizationService.listTeams().stream().map(SalesTeamResponse::from).toList();
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesTeamResponse createTeam(@RequestBody SalesTeamRequest request) {
        return SalesTeamResponse.from(organizationService.createTeam(request));
    }

    @PutMapping("/teams/{id}")
    public SalesTeamResponse updateTeam(@PathVariable Long id, @RequestBody SalesTeamRequest request) {
        return SalesTeamResponse.from(organizationService.updateTeam(id, request));
    }

    @PostMapping("/teams/{id}/retire")
    public ResponseEntity<Void> retireTeam(@PathVariable Long id) {
        organizationService.retireTeam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/territories")
    public List<SalesTerritoryResponse> listTerritories() {
        return organizationService.listTerritories().stream().map(SalesTerritoryResponse::from).toList();
    }

    @PostMapping("/territories")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesTerritoryResponse createTerritory(@RequestBody SalesTerritoryRequest request) {
        return SalesTerritoryResponse.from(organizationService.createTerritory(request));
    }

    @PutMapping("/territories/{id}")
    public SalesTerritoryResponse updateTerritory(@PathVariable Long id, @RequestBody SalesTerritoryRequest request) {
        return SalesTerritoryResponse.from(organizationService.updateTerritory(id, request));
    }

    @PostMapping("/territories/{id}/retire")
    public ResponseEntity<Void> retireTerritory(@PathVariable Long id) {
        organizationService.retireTerritory(id);
        return ResponseEntity.noContent().build();
    }
}
