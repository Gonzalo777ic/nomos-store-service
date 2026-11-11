package com.valeerp.sales;

public record SalesTeamResponse(Long id, String code, String name, Long leaderUserId, Long territoryId, String description) {

    public static SalesTeamResponse from(SalesTeam team) {
        return new SalesTeamResponse(
                team.getId(), team.getCode(), team.getName(),
                team.getLeaderUserId(), team.getTerritoryId(), team.getDescription());
    }
}
