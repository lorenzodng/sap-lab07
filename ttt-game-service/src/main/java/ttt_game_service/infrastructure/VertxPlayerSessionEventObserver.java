package ttt_game_service.infrastructure;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import common.exagonal.Adapter;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import ttt_game_service.application.PlayerSessionEventObserver;

/*
implementazione della porta di uscita che collega il servizio di gioco al client
notifica gli eventi di una partita ai giocatori
*/
@Adapter
public class VertxPlayerSessionEventObserver implements PlayerSessionEventObserver {

    static Logger logger = Logger.getLogger("[VertxEventNotifierAdapter]");
    private EventBus eventBus; //event bus di vertx
    private List<JsonObject> eventBuffer; //lista di eventi in attesa sul buffer
    private boolean channelOnBusReady; //flag che indica la disponibilità del canale sull'event bus

    public VertxPlayerSessionEventObserver(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBuffer = new LinkedList<JsonObject>();
        channelOnBusReady = false;
    }

    //notifica il client che la partita è iniziata
    public void gameStarted(String playerSessionId) {
        logger.info("game-started for " + playerSessionId);
        var evStarted = new JsonObject(); //crea un oggetto json rappresentativo dell'evento inizio partita
        evStarted.put("event", "game-started"); //aggiunge all'oggetto json il nome dell'evento
        if (channelOnBusReady) { //se il canale sull'event bus è pronto (il client è pronto a ricevere messaggi)
            eventBus.publish(playerSessionId, evStarted); //pubblica l'evento sul bus all'indirizzo corrispondente a "playerSessionId"
        } else { //altrimenti
            eventBuffer.add(evStarted); //aggiunge l'evento al buffer temporaneo per inviarlo più tardi
        }
    }

    //notifica il client di una nuova mossa eseguita
    public void newMove(String playerSessionId, String who, int x, int y) {
        var evMove = new JsonObject(); //crea un oggetto json
        evMove.put("event", "new-move"); //popola l'oggetto con l'informazione "nuova mossa"
        evMove.put("x", x); //popola l'oggetto con la coordinata "x" della mossa
        evMove.put("y", y); //popola l'oggetto con la coordinata "y" della mossa
        evMove.put("symbol", who); //popola l'oggetto con il simbolo del giocatore
        if (channelOnBusReady) { //se il canale sull'event bus è pronto (il client è pronto a ricevere messaggi)
            eventBus.publish(playerSessionId, evMove); //pubblica l'evento sul bus all'indirizzo corrispondente a "playerSessionId"
        } else { //altrimenti
            eventBuffer.add(evMove); //aggiunge l'evento al buffer temporaneo per inviarlo più tardi
        }
    }

    //notifica il client che la partita è terminata
    public void gameEnded(String playerSessionId, Optional<String> winner) {
        var evEnd = new JsonObject(); //crea un oggetto json
        evEnd.put("event", "game-ended"); //popola l'oggetto con il nome
        if (winner.isEmpty()) { //se non c'è un vincitore
            evEnd.put("result", "tie");	//popola l'oggetto con l'informazione "pareggio"
        } else { //altrimenti
            evEnd.put("winner", winner.get()); //popola l'oggetto con l'informazione del vincitore
        }
        if (channelOnBusReady) { //se il canale sull'event bus è pronto (il client è pronto a ricevere messaggi)
            eventBus.publish(playerSessionId, evEnd); //pubblica l'evento sul bus all'indirizzo corrispondente a "playerSessionId"
        } else { //altrimenti
            eventBuffer.add(evEnd); //aggiunge l'evento al buffer temporaneo per inviarlo più tardi
        }
    }

    //attiva la consegna degli eventi al client
    public void enableEventNotification(String playerSessionId) {
        channelOnBusReady = true; //segnala che il canale sull'event bus è pronto
        for (var ev: eventBuffer) { //per ogni evento presente nel buffer
            eventBus.publish(playerSessionId, ev); //pubblica ciascun evento sul bus all'indirizzo corrispondente a "playerSessionId"
        }
        eventBuffer.clear(); //svuota il buffer
    }
}