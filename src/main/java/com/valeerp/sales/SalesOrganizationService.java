package com.valeerp.sales;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.DomainExceptionHandler;
import com.valeerp.kernel.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesOrganizationService {

    private final SalesTeamRepository teamRepository;
    private final SalesTerritoryRepository territoryRepository;
    private final TimeProvider timeProvider;

    public SalesOrganizationService(SalesTeamRepository teamRepository,
                                    SalesTerritoryRepository territoryRepository,
                                    TimeProvider timeProvider) {
        this.teamRepository = teamRepository;
        this.territoryRepository = territoryRepository;
        this.timeProvider = timeProvider;
    }

    public List<SalesTeam> listTeams() {
        return teamRepository.findAll();
    }

    public SalesTeam requireTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Equipo de venta no encontrado"));
    }

    @Transactional
    public SalesTeam createTeam(SalesTeamRequest request) {
        if (request.territoryId() != null) {
            requireTerritory(request.territoryId());
        }
        return teamRepository.save(SalesTeam.create(
                request.code(), request.name(), request.leaderUserId(), request.territoryId(), request.description()));
    }

    @Transactional
    public SalesTeam updateTeam(Long id, SalesTeamRequest request) {
        SalesTeam team = requireTeam(id);
        if (request.territoryId() != null) {
            requireTerritory(request.territoryId());
        }
        team.rename(request.code(), request.name(), request.leaderUserId(), request.territoryId(), request.description());
        return teamRepository.save(team);
    }

    @Transactional
    public void retireTeam(Long id) {
        SalesTeam team = requireTeam(id);
        team.retire(Actors.currentActor(), timeProvider.now());
        teamRepository.save(team);
    }

    public List<SalesTerritory> listTerritories() {
        return territoryRepository.findAll();
    }

    public SalesTerritory requireTerritory(Long id) {
        return territoryRepository.findById(id)
                .orElseThrow(() -> new DomainException(DomainExceptionHandler.NOT_FOUND_CODE, "Territorio no encontrado"));
    }

    @Transactional
    public SalesTerritory createTerritory(SalesTerritoryRequest request) {
        return territoryRepository.save(SalesTerritory.create(
                request.code(), request.name(), request.region(), request.description()));
    }

    @Transactional
    public SalesTerritory updateTerritory(Long id, SalesTerritoryRequest request) {
        SalesTerritory territory = requireTerritory(id);
        territory.rename(request.code(), request.name(), request.region(), request.description());
        return territoryRepository.save(territory);
    }

    @Transactional
    public void retireTerritory(Long id) {
        SalesTerritory territory = requireTerritory(id);
        territory.retire(Actors.currentActor(), timeProvider.now());
        territoryRepository.save(territory);
    }
}
