package ttt_api_gateway.application;

import common.exagonal.OutBoundPort;
import io.vertx.core.Vertx;
import ttt_api_gateway.domain.*;

//interfaccia che contiene tutti i metodi che l'api gateway può richiamare per interagire il servizio di gioco
@OutBoundPort
public interface GameService {

    //recupera le informazioni di una partita
    Game getGameInfo(String gameId) throws GameNotFoundException, ServiceNotAvailableException;

    //esegue una mossa
    void makeAMove(String gameId, String playerSessionId, int x, int y) throws InvalidMoveException, ServiceNotAvailableException;

    //crea una canale per ricevere gli eventi di gioco
    void createAnEventChannel(String playerSessionId, Vertx vertx);

}
