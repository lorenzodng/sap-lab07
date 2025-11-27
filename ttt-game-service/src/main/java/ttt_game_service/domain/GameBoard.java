package ttt_game_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import common.ddd.Entity;

//griglia di gioco
public class GameBoard implements Entity<String>{

    public enum BoardCellContentType {CROSS, CIRCLE, EMPTY}; //enum per i simboli delle caselle
    private BoardCellContentType[][] board; //matrice che rappresenta la griglia di caselle
    private int numFreeCellsLeft; //numero di caselle disponibili
    private String id; //id della griglia

    public GameBoard(String id) {
        this.id = id;
        board = new BoardCellContentType[3][3];
        for (int y = 0; y < 3; y++) { //per ogni riga
            for (int x = 0; x < 3; x++) { //per ogni colonna
                board[y][x] = BoardCellContentType.EMPTY; //inizializza lo stato della cella come vuoto
            }
        }
        numFreeCellsLeft = 9;
    }

    //esegue una mossa
    public void newMove(TTTSymbol symbol, int x, int y) throws InvalidMoveException {
        if (board[y][x].equals(BoardCellContentType.EMPTY)) { //se la casella indicata è vuota
            board[y][x] = symbol.equals(TTTSymbol.X) ? BoardCellContentType.CROSS : BoardCellContentType.CIRCLE; //inserisce il simbolo nella casella
            numFreeCellsLeft--; //decrementa il numero di caselle disponibili
        } else { //altrimenti
            throw new InvalidMoveException(); //lancia un'eccezione
        }
    }

    //controlla la presenza di un vincitore
    public Optional<TTTSymbol> checkWinner(){
        for (int y = 0; y < 3; y++) { //per tutte le righe della griglia
            if (!board[y][0].equals(BoardCellContentType.EMPTY) && board[y][0].equals(board[y][1]) && board[y][1].equals(board[y][2])){ //se tutte e tre le caselle sono uguali
                return board[y][0].equals(BoardCellContentType.CROSS) ? Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O); //restituisce il simbolo presente nella prima casella della riga
            }
        }

        for (int x = 0; x < 3; x++) { //per tutte le colonne della griglia
            if (!board[0][x].equals(BoardCellContentType.EMPTY) && board[0][x].equals(board[1][x]) && board[1][x].equals(board[2][x])){ //se tutte e tre le caselle sono uguali
                return board[0][x].equals(BoardCellContentType.CROSS) ? Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O); //restituisce il simbolo presente nella prima casella della colonna
            }
        }

        if (!board[0][0].equals(BoardCellContentType.EMPTY) && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])){ //se tutte e tre le caselle della diagonale principale della griglia sono uguali
            return board[0][0].equals(BoardCellContentType.CROSS) ? Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O); //restituisce il simbolo presente nella prima casella della diagonale
        }

        if (!board[2][0].equals(BoardCellContentType.EMPTY) && board[2][0].equals(board[1][1]) && board[1][1].equals(board[0][2])){ //se tutte e tre le caselle della diagonale secondaria della griglia sono uguali
            return board[2][0].equals(BoardCellContentType.CROSS) ? Optional.of(TTTSymbol.X) : Optional.of(TTTSymbol.O); //restituisce il simbolo presente nella prima casella della diagonale
        }

        return Optional.empty(); //altrimenti non ritorna nessun simbolo
    }

    //recupera lo stato della griglia
    public List<String> getState() {
        var l = new ArrayList<String>(); //lista di simboli
        for (int y = 0; y < 3; y++) { //per ogni riga
            for (int x = 0; x < 3; x++) { //per ogni colonna
                if (board[y][x].equals(BoardCellContentType.CROSS)) { //se la casella contiene una X
                    l.add("X"); //aggiunge "X" alla lista
                } else if (board[y][x].equals(BoardCellContentType.CIRCLE)) { //altrimenti se la casella contiene un O
                    l.add("O"); //aggiunge "O" alla lista
                } else { //altrimenti
                    l.add("-"); //aggiunge "-" alla lista
                }
            }
        }
        return l; //restituisce la lista
    }

    //verifica il pareggio
    public boolean isTie() {
        return numFreeCellsLeft == 0;
    }

    @Override
    public String getId() {
        return id;
    }
}

