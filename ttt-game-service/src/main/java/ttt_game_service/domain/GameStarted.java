package ttt_game_service.domain;

//record per la partita avviata
public record GameStarted (String gameId) implements GameEvent {}
