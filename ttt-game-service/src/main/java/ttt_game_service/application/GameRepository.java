package ttt_game_service.application;

import common.ddd.Repository;
import common.exagonal.OutBoundPort;
import ttt_game_service.domain.Game;

/*
interfaccia che collega l'architettura (applicazione) al db delle partite
contiene tutti i metodi che l'architettura utilizza per interagire con il db delle partite
*/
@OutBoundPort
public interface GameRepository extends Repository {

    //aggiunge una partita
    void addGame(Game game);

    //verifica se una partita è presente
    boolean isPresent(String gameId);

    //recupera una partita
    Game getGame(String gameId);

}
