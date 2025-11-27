package ttt_lobby_service.application;

import common.exagonal.InBoundPort;
import ttt_lobby_service.domain.TTTSymbol;

//interfaccia che contiene tutti i metodi che il client può richiamare per interagire con la lobby
@InBoundPort
public interface LobbyService  {

    //logga l'utente
    String login(String userName, String password) throws LoginFailedException;

    //crea una nuova partita
    void createNewGame(String sessionId, String gameId) throws CreateGameFailedException;

    //fa entrare l'utente in una partita
    String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException;
}
