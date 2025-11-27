package ttt_game_service.domain;

//interfaccia observer per gli eventi di gioco
public interface GameObserver {

    void notifyGameEvent(GameEvent event);
}
