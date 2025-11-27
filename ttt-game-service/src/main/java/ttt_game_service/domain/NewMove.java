package ttt_game_service.domain;

//record per la mossa
public record NewMove (String gameId, String symbol, int x, int y) implements GameEvent {}
