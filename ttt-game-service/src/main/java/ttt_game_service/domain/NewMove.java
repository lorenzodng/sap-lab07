package ttt_game_service.domain;

/**
 * 
 * Domain event: new move
 * 
 */

public record NewMove (String gameId, String symbol, int x, int y) implements GameEvent {}
