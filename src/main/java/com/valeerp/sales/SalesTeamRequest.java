package com.valeerp.sales;

public record SalesTeamRequest(String code, String name, Long leaderUserId, Long territoryId, String description) {
}
