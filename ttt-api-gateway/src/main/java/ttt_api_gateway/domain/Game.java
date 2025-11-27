package ttt_api_gateway.domain;

import java.util.List;

//record per la partita
public record Game (String gameId, String gameState, List<String> boardState, String currentTurn) {}
