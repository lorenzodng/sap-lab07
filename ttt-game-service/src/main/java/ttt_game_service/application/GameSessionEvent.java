package ttt_game_service.application;

import ttt_game_service.domain.GameEvent;

public record GameSessionEvent(String sessionId, GameEvent ev) {

}
