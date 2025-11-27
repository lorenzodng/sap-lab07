package ttt_game_service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.ddd.Aggregate;

//partita in corso
public class Game implements Aggregate<String>{

    static Logger logger = Logger.getLogger("[Game]");
    private String id; //id della partita
    private GameBoard board; //griglia della partita
    public enum GameState {WAITING_FOR_PLAYERS, STARTED, FINISHED} //enum per lo stato della partita
    private GameState state; //stato della partita
    private Optional<UserId> playerCross; //id del giocatore X
    private Optional<UserId> playerCircle; //id del giocatore O
    private Optional<UserId> winner; //vincitore
    private Optional<UserId> currentTurn; //turno corrente
    private List<GameObserver> observers; //lista degli osservatori (utenti) della partita

    public Game(String id) {
        this.id = id;
        board = new GameBoard(id+"-board");
        playerCross = Optional.empty();
        playerCircle = Optional.empty();
        currentTurn = Optional.empty();
        winner = Optional.empty();
        state = GameState.WAITING_FOR_PLAYERS; //inizializza lo stato della partita come "in attesa"
        observers = new ArrayList<>();
    }

    //restituisce l'id della partita
    public String getId() {
        return id;
    }

    //fa entrare un utente nella partita
    public void joinGame(UserId userId, TTTSymbol symbol) throws InvalidJoinException {
        if (!state.equals(GameState.WAITING_FOR_PLAYERS) || (symbol.equals(TTTSymbol.X) && playerCross.isPresent()) || (symbol.equals(TTTSymbol.O) && playerCircle.isPresent())) { //se la partita non è "in attesa" o se l'utente sceglie "croce" e il simbolo è già stato selezionato, se l'utente sceglie "cerchio" e il simbolo è già stato selezionato
            throw new InvalidJoinException(); //lancia un'eccezione
        }

        //(altrimenti)

        if (symbol.equals(TTTSymbol.X)) { //se l'utente sceglie "croce"
            playerCross = Optional.of(userId); //assegna al giocatore il simbolo "croce"
        } else { //altrimenti
            playerCircle = Optional.of(userId); //assegna al giocatore il simbolo "cerchio"
        }
    }

    //avvia la partita
    public void startGame() {
        state = GameState.STARTED; //imposta lo stato della partita come "avviata"
        currentTurn = playerCross; //inizializza il turno al giocatore "croce"
        notifyGameEvent(new GameStarted(id));  //invia un evento di avvio della partita a tutti gli osservatori (giocatori) registrati alla partita
    }

    //recupera lo stato della griglia
    public List<String> getBoardState(){
        return this.board.getState();
    }

    //recupera il turno corrente
    public String getCurrentTurn() {
        if (currentTurn == playerCross) {
            return "X";
        } else {
            return "O";
        }
    }

    //verifica se la partita è iniziata
    public boolean isStarted() {
        return state.equals(GameState.STARTED);
    }

    //verifica se la partita è terminata
    public boolean isFinished() {
        return state.equals(GameState.FINISHED);
    }

    //recupera lo stato della partita
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

    //verifica se la partita può iniziare
    public boolean isReadyToStart() {
        return (playerCross.isPresent() && playerCircle.isPresent());
    }

    //esegue una mossa
    public void makeAmove(UserId userId, int x, int y) throws InvalidMoveException {
        logger.log(Level.INFO, "new move by " + userId.id() + " in (" + x + ", " + y + ")");
        UserId p = currentTurn.get(); //recupera il turno corrente
        if (userId.id().equals(p.id())) { //se l'utente (che chiama il metodo) è il giocatore associato al turno corrente
            var gridSymbol = userId.id().equals(playerCross.get().id()) ? TTTSymbol.X : TTTSymbol.O; //se il giocatore ha simbolo "croce", memorizza il simbolo croce, altrimenti memorizza il simbolo "cerchio"
            board.newMove(gridSymbol, x, y); //esegue la mossa
            notifyGameEvent(new NewMove(id, gridSymbol.toString(), x, y)); //invia un evento di esecuzione della mossa a tutti gli osservatori (giocatori) registrati alla partita

            currentTurn = (currentTurn == playerCross) ? playerCircle : playerCross; //cambia il turno
            var optWin = board.checkWinner(); //verifica la presenza di un vincitore
            if (optWin.isPresent()) { //se è presente un vincitore
                winner = Optional.of(getPlayerUsingSymbol(optWin.get())); //recupera il vincitore
                state = GameState.FINISHED; //imposta lo stato della partita come "terminato"
                notifyGameEvent(new GameEnded(id, Optional.of(winner.get().id())));
            } else if (board.isTie()) { //se è finita in parità
                state = GameState.FINISHED; //imposta lo stato della partita come "terminato"
                notifyGameEvent(new GameEnded(id, Optional.empty())); //invia un evento di terminazione della partita a tutti gli osservatori (giocatori) registrati alla partita
            }
        } else { //altrimenti
            throw new InvalidMoveException(); //lancia un'eccezione
        }
    }

    //aggiunge un osservatore alla partita
    public void addGameObserver(GameObserver observer) {
        observers.add(observer);
    }

    //invia un evento di notifica agli osservatori
    private void notifyGameEvent(GameEvent ev) {
        for (var o: observers) { //per ogni elemento nella lista degli osservatori
            o.notifyGameEvent(ev); //notifica l'osservatore
        }
    }

    //recupera l'utente dal suo simbolo
    private UserId getPlayerUsingSymbol(TTTSymbol symbol) {
        if (symbol.equals(TTTSymbol.X)) { //se il simbolo è "croce"
            return playerCross.get(); //restituisce l'id del giocatore croce
        } else { //altrimenti
            return playerCircle.get(); //restituisce l'id del giocatore "cerchio"
        }
    }
}

