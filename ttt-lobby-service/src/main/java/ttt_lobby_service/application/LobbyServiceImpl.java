package ttt_lobby_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import ttt_lobby_service.domain.*;


/**
 * 
 * Implementation of the Game Service entry point at the application layer
 * 
 * Designed as a modular monolith
 * 
 */
public class LobbyServiceImpl implements LobbyService {
	static Logger logger = Logger.getLogger("[Lobby Service]");
    
    private UserSessions userSessionRepository;
    private int sessionCount;
    private AccountService accountService;
    private GameService gameService;
    
    public LobbyServiceImpl(){
    	userSessionRepository = new UserSessions();
    	sessionCount = 0;
    }
    
    @Override
	public String login(String userName, String password) throws LoginFailedException {
		logger.log(Level.INFO, "Login: " + userName + " " + password);
		try {
			if (!accountService.isValidPassword(userName, password)) {
				throw new LoginFailedException();
			}			
			var id = new UserId(userName);
			sessionCount++;
			var sessionId = "user-session-" + sessionCount;
			var us = new UserSession(sessionId, id, this);
			userSessionRepository.addSession(us);			
			return us.getSessionId();
		} catch (UserNotFoundException ex) {
			throw new LoginFailedException();
		} catch (ServiceNotAvailableException ex) {
			throw new LoginFailedException();
		}
	}

	
	@Override
	public void createNewGame(String sessionId, String gameId) throws CreateGameFailedException {
		logger.log(Level.INFO, "create new game " + sessionId + " " + gameId);
		try {			
			if (userSessionRepository.isPresent(sessionId)) {		
				gameService.createNewGame(gameId);
			} else {
				throw new CreateGameFailedException();
			}
		} catch (ServiceNotAvailableException ex) {
			throw new CreateGameFailedException();
		} catch (GameAlreadyPresentException ex) {
			throw new CreateGameFailedException();
		}		
	}

	@Override
	public String joinGame(String sessionId, String gameId, TTTSymbol symbol) throws JoinGameFailedException  {
		logger.log(Level.INFO, "join game " + sessionId + " " + gameId);
		try {			
			if (userSessionRepository.isPresent(sessionId)) {		
				var us = userSessionRepository.getSession(sessionId);
				return gameService.joinGame(us.getUserId(), gameId, symbol);
			} else {
				throw new InvalidJoinGameException();
			}
		} catch (InvalidJoinGameException ex) {
			throw new JoinGameFailedException();
		} catch (ServiceNotAvailableException ex) {
			throw new JoinGameFailedException();
		}		
	}
	    
	public void bindAccountService(AccountService service) {
		this.accountService = service;
	}

	public void bindGameService(GameService service) {
		this.gameService = service;
	}


}
