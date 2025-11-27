package ttt_game_service.application;

import java.util.Optional;
import common.exagonal.OutBoundPort;

/*
interfaccia observer che collega l'architettura (applicazione) al client
contiene tutti i metodi che l'architettura utilizza per notificare il giocatore
 */
@OutBoundPort
public interface PlayerSessionEventObserver {

    //notifica che l'observer è pronto a ricevere gli eventi
    void enableEventNotification(String playerSessionId);

    //notifica che la partita è iniziata
    void gameStarted(String playerSessionId);

    //notifica che un giocatore ha eseguito una mossa
    void newMove(String playerSessionId, String symbol, int x, int y);

    //notifica che la partita è terminata
    void gameEnded(String playerSessionId, Optional<String> winner);
}

