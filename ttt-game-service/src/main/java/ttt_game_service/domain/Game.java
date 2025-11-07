package ttt_game_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.ddd.Aggregate;


/**
 * 
 * Modelling a running game.
 * 
 */
public class Game implements Aggregate<String>{
	static Logger logger = Logger.getLogger("[Game]");

	private String id;
	private GameBoard board;

	public enum GameState { WAITING_FOR_PLAYERS, STARTED, FINISHED }
	private GameState state;
	
	private Optional<UserId> playerCross;  /* joined player with cross */
	private Optional<UserId> playerCircle; /* joined player with circle */
	private Optional<UserId> winner;		
	private Optional<UserId> currentTurn;
	
	private List<GameObserver> observers; /* observers of game events */
	
	public Game(String id) {
		this.id = id;
		board = new GameBoard(id+"-board");

		playerCross = Optional.empty();
		playerCircle = Optional.empty();
		currentTurn = Optional.empty();		
		winner = Optional.empty();
		state = GameState.WAITING_FOR_PLAYERS;
		observers = new ArrayList<>();		
	}	
	
	public String getId() {
		return id;
	}
		
	/**
	 * 
	 * A player joins a game
	 * 
	 * @param user
	 * @param symbol
	 * @throws InvalidJoinException
	 */
	public void joinGame(UserId userId, TTTSymbol symbol) throws InvalidJoinException {
		if (!state.equals(GameState.WAITING_FOR_PLAYERS) || 
		    (symbol.equals(TTTSymbol.X) && playerCross.isPresent()) ||
			(symbol.equals(TTTSymbol.O) && playerCircle.isPresent())) {
			throw new InvalidJoinException();
		} 
		
		if (symbol.equals(TTTSymbol.X)) {
			playerCross = Optional.of(userId);
		} else {
			playerCircle = Optional.of(userId);
		}
	}
	
	
	/**
	 * 
	 * Starts the game.
	 * 
	 * The game is started after that both players joined the game.
	 * 
	 */
	public void startGame() {
		state = GameState.STARTED;
		currentTurn = playerCross;
		notifyGameEvent(new GameStarted(id));				
	}
	
	/**
	 * Get the board state
	 * 
	 * @return
	 */
	public List<String> getBoardState(){
		return this.board.getState();
	}
	
	/**
	 * 
	 * Get current turn
	 * 
	 * @return
	 */
	public String getCurrentTurn() {
		if (currentTurn == playerCross) {
			return "X";
		} else {
			return "O";
		}
	}

	/**
	 * 
	 * Check if the game has started
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return state.equals(GameState.STARTED);
	}

	/**
	 * 
	 * Check if the game is ended
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return state.equals(GameState.FINISHED);
	}
	
	/**
	 * Get the game state
	 * 
	 * @return
	 */
	public String getGameState() {
		if (state.equals(GameState.WAITING_FOR_PLAYERS)) {
			return "waiting-for-players";
		} else if (state.equals(GameState.STARTED)) {
			return "started";
		} else if (state.equals(GameState.FINISHED)) {
			return "finished";
		} else {
			return "unknown";
		}
	}
	
	/**
	 * 
	 * Checks if the game can start.
	 * 
	 * @return
	 */
	public boolean isReadyToStart() {
		return (playerCross.isPresent() && playerCircle.isPresent());
	}
	
	
	/**
	 * 
	 * A player makes a move
	 * 
	 * @param UserId
	 * @param symbol
	 * @param x
	 * @param y
	 * @throws InvalidMoveException
	 */
	public void makeAmove(UserId userId, int x, int y) throws InvalidMoveException {
		logger.log(Level.INFO, "new move by " + userId.id() + " in (" + x + ", " + y + ")");
		UserId p = currentTurn.get();
		if (userId.id().equals(p.id())) {
			
			var gridSymbol = userId.id().equals(playerCross.get().id()) ?
						TTTSymbol.X : TTTSymbol.O;
			
			board.newMove(gridSymbol, x, y);
			notifyGameEvent(new NewMove(id, gridSymbol.toString(), x, y));				

			/* check state */ 
			
			currentTurn = (currentTurn == playerCross) ? playerCircle : playerCross;
			var optWin = board.checkWinner();
			if (optWin.isPresent()) {
				winner = Optional.of(getPlayerUsingSymbol(optWin.get()));
				state = GameState.FINISHED;
				notifyGameEvent(new GameEnded(id, Optional.of(winner.get().id())));				
			} else if (board.isTie()) {
				state = GameState.FINISHED;
				notifyGameEvent(new GameEnded(id, Optional.empty()));				
			}				
		} else {
			throw new InvalidMoveException();			
		}
	}
	
	/**
	 * 
	 * Adding an observer to notify game events
	 * 
	 * @param observer
	 */
	public void addGameObserver(GameObserver observer) {
		observers.add(observer);
	}
	
	private void notifyGameEvent(GameEvent ev) {
		for (var o: observers) {
			o.notifyGameEvent(ev);				
		}
	}
		
	private UserId getPlayerUsingSymbol(TTTSymbol symbol) {
		if (symbol.equals(TTTSymbol.X)) {
			return playerCross.get();
		} else {
			return playerCircle.get();
		}
	}
}
