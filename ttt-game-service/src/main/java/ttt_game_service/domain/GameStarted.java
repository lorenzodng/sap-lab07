package ttt_game_service.domain;

/**
 * 
 * Domain event: game started
 * 
 */
public record GameStarted (String gameId) implements GameEvent {}
