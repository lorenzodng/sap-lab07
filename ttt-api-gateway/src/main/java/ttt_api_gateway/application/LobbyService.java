package ttt_api_gateway.application;

//interfaccia che contiene tutti i metodi che l'api gateway può richiamare per interagire il servizio di lobby
public interface LobbyService  {

    //logga l'utente
	String login(String userName, String password) throws LoginFailedException, ServiceNotAvailableException;

    //crea una nuova partita
	void createNewGame(String sessionId, String gameId) throws CreateGameFailedException, ServiceNotAvailableException;

    //fa entrare l'utente in una partita
	String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException, ServiceNotAvailableException;

}
