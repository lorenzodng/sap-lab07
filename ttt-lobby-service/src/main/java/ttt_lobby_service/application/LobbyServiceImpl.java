package ttt_lobby_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;
import ttt_lobby_service.domain.*;

//servizio di lobby
public class LobbyServiceImpl implements LobbyService {

    static Logger logger = Logger.getLogger("[Lobby Service]");
    private UserSessions userSessionRepository; //repository delle sessioni degli utenti
    private int sessionCount; //numero di sessioni utente
    private AccountService accountService; //servizio di gestione account per la lobby
    private GameService gameService; //servizio di gestione partite per la lobby

    public LobbyServiceImpl(){
        userSessionRepository = new UserSessions();
        sessionCount = 0;
    }

    //esegue il login di un utente al servizio di lobby
    @Override
    public String login(String userName, String password) throws LoginFailedException {
        logger.log(Level.INFO, "Login: " + userName + " " + password);
        try {
            if (!accountService.isValidPassword(userName, password)) { //se i dati non sono corretti
                throw new LoginFailedException(); //lancia un'eccezione
            }
            var id = new UserId(userName); //crea l'utente
            sessionCount++; //incrementa il numero di sessioni utente
            var sessionId = "user-session-" + sessionCount; //crea un id per la sessione
            var us = new UserSession(sessionId, id, this); //crea la sessione
            userSessionRepository.addSession(us); //aggiunge la sessione
            return us.getSessionId(); //restituisce la sessione creata
        } catch (UserNotFoundException | ServiceNotAvailableException ex) {
            throw new LoginFailedException();
        }
    }

    //crea una nuova partita
    @Override
    public void createNewGame(String sessionId, String gameId) throws CreateGameFailedException {
        logger.log(Level.INFO, "create new game " + sessionId + " " + gameId);
        try {
            if (userSessionRepository.isPresent(sessionId)) { //se la sessione dell'utente esiste
                gameService.createNewGame(gameId); //crea una partita
            } else { //altrimenti
                throw new CreateGameFailedException(); //lancia un'eccezione
            }
        } catch (ServiceNotAvailableException | GameAlreadyPresentException ex) {
            throw new CreateGameFailedException();
        }
    }

    //fa entrare un utente in una partita
    @Override
    public String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException  {
        logger.log(Level.INFO, "join game " + sessionId + " " + gameId);
        try {
            if (userSessionRepository.isPresent(sessionId)) { //se la sessione dell'utente esiste
                var us = userSessionRepository.getSession(sessionId); //recupera la sessione
                return gameService.joinGame(us.getUserId(), gameId, symbol); //fa entrare l'utente nella partita indicata
            } else { //altrimenti
                throw new InvalidJoinGameException(); //lancia un'eccezione
            }
        } catch (InvalidJoinGameException | ServiceNotAvailableException ex) {
            throw new JoinGameFailedException();
        }
    }

    //definisce un servizio di gestione account
    public void bindAccountService(AccountService service) {
        this.accountService = service;
    }

    //definisce un servizio di gestione partite
    public void bindGameService(GameService service) {
        this.gameService = service;
    }

}