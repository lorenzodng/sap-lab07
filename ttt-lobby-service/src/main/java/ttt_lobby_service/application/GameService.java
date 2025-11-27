package ttt_lobby_service.application;

import common.exagonal.OutBoundPort;
import ttt_lobby_service.domain.*;

//interfaccia che contiene tutti i metodi che la lobby può richiamare per interagire con il servizio di gioco
@OutBoundPort
public interface GameService  {

    //crea una nuova partita
    void createNewGame(String gameId) throws GameAlreadyPresentException, CreateGameFailedException, ServiceNotAvailableException;

    //fa entrare un utente in una partita
    String joinGame(UserId userId, String gameId, TTTSymbol symbol) throws InvalidJoinGameException, JoinGameFailedException, ServiceNotAvailableException;
}
