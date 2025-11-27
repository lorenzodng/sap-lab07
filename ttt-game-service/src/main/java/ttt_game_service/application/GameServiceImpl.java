package ttt_game_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;
import ttt_game_service.domain.*;

//servizio applicativo che gestisce il flusso di gioco
public class GameServiceImpl implements GameService {

    static Logger logger = Logger.getLogger("[Game Service]");
    private GameRepository gameRepository; //repository delle partite in corso
    private PlayerSessions playerSessionRepository; //repository delle sessioni dei giocatori
    private int playerSessionCount; //numero di sessioni giocatore

    public GameServiceImpl(){
        playerSessionRepository = new PlayerSessions();
        playerSessionCount = 0;
    }

    //recupera una sessione
    @Override
    public PlayerSession getPlayerSession(String sessionId) {
        return playerSessionRepository.getSession(sessionId);
    }

    //crea una nuova partita
    @Override
    public void createNewGame(String gameId) throws GameAlreadyPresentException {
        logger.log(Level.INFO, "create New Game " + gameId);
        var game = new Game(gameId); //crea una partita
        if (gameRepository.isPresent(gameId)) { //se la partita esiste (è già avviata)
            throw new GameAlreadyPresentException(); //lancia un'eccezione
        }
        gameRepository.addGame(game); //avvia la partita
    }

    //recupera una partita
    @Override
    public Game getGameInfo(String gameId) throws GameNotFoundException {
        logger.log(Level.INFO, "create New Game " + gameId);
        if (!gameRepository.isPresent(gameId)) { //se la partita non esiste (non è già avviata)
            throw new GameNotFoundException(); //lancia un'eccezione
        }
        return gameRepository.getGame(gameId); //recupera la partita
    }

    //fa entrare un utente in una partita
    @Override
    public PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol, PlayerSessionEventObserver notifier) throws InvalidJoinException {
        logger.log(Level.INFO, "JoinGame - user: " + userId + " game: " + gameId + " symbol " + symbol);
        var game = gameRepository.getGame(gameId); //recupera la partita
        game.joinGame(userId, symbol); //fa entrare l'utente nella partita indicata
        playerSessionCount++; //incrementa il numero di sessioni giocatore
        var playerSessionId = "player-session-" + playerSessionCount; //crea un id per la sessione
        var ps = new PlayerSession(playerSessionId, userId, game, symbol);  //crea la sessione
        ps.bindPlayerSessionEventNotifier(notifier); //definisce un observer per la sessione
        playerSessionRepository.addSession(ps); //aggiunge la sessione
        game.addGameObserver(ps); //aggiunge l'observer
        if (game.isReadyToStart()) { //se la partita può iniziare
            game.startGame(); //avvia la partita
        }
        return ps; //restituisce la sessione giocatore
    }

    //definisce un repository per le partite
    public void bindGameRepository(GameRepository repo) {
        this.gameRepository = repo;
    }
}
