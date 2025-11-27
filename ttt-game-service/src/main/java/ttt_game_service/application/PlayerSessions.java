package ttt_game_service.application;

import java.util.HashMap;
import common.ddd.Repository;

//tiene traccia delle sessioni di tutti i giocatori
public class PlayerSessions implements Repository {

    private HashMap<String, PlayerSession> userSessions; //hashmap che associa il giocatore alla sessione

    public PlayerSessions() {
        userSessions = new HashMap<>();
    }

    //aggiunge una sessione
    public void addSession(PlayerSession ps) {
        userSessions.put(ps.getId(), ps);
    }

    //recupera la sessione del giocatore
    public PlayerSession getSession(String sessionId) {
        return userSessions.get(sessionId);
    }

}
