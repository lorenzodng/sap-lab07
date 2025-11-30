package ttt_game_service.application;

import ttt_game_service.domain.*;

//sessione del giocatore
public class PlayerSession implements GameObserver {

    private UserId userId; //id del giocatore
    private Game game; //partita associata alla sessione
    private final TTTSymbol symbol; //simbolo del giocatore (X o O)
    private String playerSessionId; //id della sessione del giocatore
    private PlayerSessionEventObserver playerSessionEventNotifier; //observer associato alla sessione del giocatore (notifica il giocatore associato a questa sessione)

    public PlayerSession(String playerSessionId, UserId userId, Game game, TTTSymbol symbol) {
        this.userId = userId;
        this.game = game;
        this.symbol = symbol;
        this.playerSessionId = playerSessionId;
    }

    //fa eseguire al giocatore una mossa
    public void makeMove(int x, int y) throws InvalidMoveException {
        game.makeAmove(userId, x, y);
    }

    //recupera l'id della sessione
    public String getId() {
        return playerSessionId;
    }

    //notifica gli eventi di gioco
    public void notifyGameEvent(GameEvent ev) {
        if (ev instanceof GameStarted) { //se l'evento è di tipo "GameStarted"
            playerSessionEventNotifier.gameStarted(playerSessionId); //notifica al giocatore che la partita è iniziata
        } else if (ev instanceof GameEnded) { //altrimenti se l'evento è di tipo "GameEnded"
            var e = (GameEnded) ev;
            playerSessionEventNotifier.gameEnded(playerSessionId, e.winner()); //notifica al giocatore che la partita è terminata e il vincitore
        } else if (ev instanceof NewMove) { //altrimenti se l'evento è di tipo "NewMove"
            var e = (NewMove) ev;
            log("new move: " + e.symbol() + " in (" + e.x() + ", " + e.y() + ")");
            playerSessionEventNotifier.newMove(playerSessionId, e.symbol(), e.x(), e.y()); //notifica al giocatore che la mossa eseguita dall'avversario
        }
    }

    //definisce un observer per la sessione
    public void bindPlayerSessionEventNotifier(PlayerSessionEventObserver playerSessionEventNotifier) {
        this.playerSessionEventNotifier = playerSessionEventNotifier;
    }

    //restituisce l'id della sessione
    public PlayerSessionEventObserver getPlayerSessionEventNotifier() {
        return playerSessionEventNotifier;
    }

    private void log(String msg) {
        System.out.println("[ player " + userId.id() + " in game " + game.getId() + " ] " + msg);
    }
}
