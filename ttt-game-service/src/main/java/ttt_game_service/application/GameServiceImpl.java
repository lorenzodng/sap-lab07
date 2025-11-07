package ttt_game_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import ttt_game_service.domain.*;


/**
 * 
 * Implementation of the Game Service entry point at the application layer
 * 
 * Designed as a modular monolith
 * 
 */
public class GameServiceImpl implements GameService {
	static Logger logger = Logger.getLogger("[Game Service]");

    private GameRepository gameRepository;    
    
    private PlayerSessions playerSessionRepository;
    private int playerSessionCount;
    
    public GameServiceImpl(){
    	playerSessionRepository = new PlayerSessions();
    	playerSessionCount = 0;
    }
    
	/**
	 * 
	 * Retrieve an existing player session.
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public PlayerSession getPlayerSession(String sessionId) {
		return playerSessionRepository.getSession(sessionId);
	}

	
	/* 
	 * 
	 * Create a game -- called by a UserSession  
	 * 
	 */
	@Override
	public void createNewGame(String gameId) throws GameAlreadyPresentException {
		logger.log(Level.INFO, "create New Game " + gameId);
		var game = new Game(gameId);
		if (gameRepository.isPresent(gameId)) {
			throw new GameAlreadyPresentException();
		}
		gameRepository.addGame(game);
	}
	
	/**
	 * 
	 * Get game info
	 * 
	 */
	@Override
	public Game getGameInfo(String gameId) throws GameNotFoundException {
		logger.log(Level.INFO, "create New Game " + gameId);
		if (!gameRepository.isPresent(gameId)) {
			throw new GameNotFoundException();
		}
		return gameRepository.getGame(gameId);
	}

	
	/*
	 * 
	 * Join a game -- called by a UserSession (logged in user), creates a new PlayerSession
	 * 
	 */
	@Override
	public PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol, PlayerSessionEventObserver notifier) throws InvalidJoinException  {
		logger.log(Level.INFO, "JoinGame - user: " + userId + " game: " + gameId + " symbol " + symbol);
		var game = gameRepository.getGame(gameId);
		game.joinGame(userId, symbol);	
		playerSessionCount++;
		var playerSessionId = "player-session-" + playerSessionCount;
		var ps = new PlayerSession(playerSessionId, userId, game, symbol);		
		ps.bindPlayerSessionEventNotifier(notifier);
		playerSessionRepository.addSession(ps);
		game.addGameObserver(ps);
		
		/* 
		 * Once both players (sessions) are ready to observe
		 * events, then we can start the game 
		 */
		if (game.isReadyToStart()) {
			game.startGame();
		}
		return ps;
	}
	
    public void bindGameRepository(GameRepository repo) {
    	this.gameRepository = repo;
    }
}
