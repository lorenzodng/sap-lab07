package ttt_game_service.application;

import common.exagonal.InBoundPort;
import ttt_game_service.domain.*;

//interfaccia che contiene tutti i metodi che il client può richiamare per interagire con il servizio di gioco
@InBoundPort
public interface GameService  {

    //recupera una partita
    Game getGameInfo(String gameId) throws GameNotFoundException;

    //recupera una sessione
    PlayerSession getPlayerSession(String sessionId);

    //crea una nuova partita
    void createNewGame(String gameId) throws GameAlreadyPresentException;

    //esegue il join di un utente ad un partita
    PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol, PlayerSessionEventObserver observer) throws InvalidJoinException;

}

