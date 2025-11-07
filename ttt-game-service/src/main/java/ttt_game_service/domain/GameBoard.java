package ttt_game_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.ddd.Entity;

/**
 * 
 * The game board of a game.
 * 
 */
public class GameBoard implements Entity<String>{

	public enum BoardCellContentType { CROSS, CIRCLE, EMPTY};
	private BoardCellContentType[][] board;
	private int numFreeCellsLeft;
	private String id;
	
	public GameBoard(String id) {
		this.id = id;
		board = new BoardCellContentType[3][3];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				board[y][x] = BoardCellContentType.EMPTY;
			}
		}		
		numFreeCellsLeft = 9;
	}

	/**
	 * 
	 * Make a new move.
	 * 
	 * @param symbol
	 * @param x
	 * @param y
	 * @throws InvalidMoveException
	 */
	public void newMove(TTTSymbol symbol, int x, int y) throws InvalidMoveException {
		if (board[y][x].equals(BoardCellContentType.EMPTY)) {
			board[y][x] = symbol.equals(TTTSymbol.X) ? BoardCellContentType.CROSS : BoardCellContentType.CIRCLE;
			numFreeCellsLeft--;
		} else {
			throw new InvalidMoveException();
		}
	}
	
	/**
	 * 
	 * Check for winners.
	 * 
	 * @return
	 */
	public Optional<TTTSymbol> checkWinner(){
			for (int y = 0; y < 3; y++) {
				if (!board[y][0].equals(BoardCellContentType.EMPTY) && 
					board[y][0].equals(board[y][1]) && 
					board[y][1].equals(board[y][2])){
					return board[y][0].equals(BoardCellContentType.CROSS) ? 
							Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O);
				}
			}
			for (int x = 0; x < 3; x++) {
				if (!board[0][x].equals(BoardCellContentType.EMPTY) && 	
					board[0][x].equals(board[1][x]) && 
					board[1][x].equals(board[2][x])){
					return board[0][x].equals(BoardCellContentType.CROSS) ? 
							Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O);
				}
			}
			if (!board[0][0].equals(BoardCellContentType.EMPTY) && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])){
				return board[0][0].equals(BoardCellContentType.CROSS) ? 
						Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O);
			}
			if (!board[2][0].equals(BoardCellContentType.EMPTY) && board[2][0].equals(board[1][1]) && board[1][1].equals(board[0][2])){
				return board[2][0].equals(BoardCellContentType.CROSS) ? 
						Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O);
			}
			return Optional.empty();
	}
	
	/**
	 * 
	 * Get board state;
	 * 
	 * @return
	 */
	public List<String> getState() {
		var l = new ArrayList<String>();
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (board[y][x].equals(BoardCellContentType.CROSS)) {
					l.add("X");
				} else if (board[y][x].equals(BoardCellContentType.CIRCLE)) {
					l.add("O");
				} else {
					l.add("-");
				}
			}
		}
		return l;
	}
		
	/**
	 * 
	 * Check for tie.
	 * 
	 * @return
	 */
	public boolean isTie() {
		return numFreeCellsLeft == 0; /* could be improved */
	}

	@Override
	public String getId() {
		return id;
	}
}

