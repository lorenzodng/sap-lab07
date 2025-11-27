package ttt_game_service.infrastructure;

import java.util.HashMap;
import common.exagonal.Adapter;
import ttt_game_service.application.GameRepository;
import ttt_game_service.domain.Game;

//implementazione della porta di uscita he collega l'architettura (applicazione) al db delle partite
@Adapter
public class InMemoryGameRepository implements GameRepository {

    private HashMap<String, Game> games; //hashmap che associa l'id della partita alla partita

    public InMemoryGameRepository() {
        games = new HashMap<>();
    }

    //aggiunge una partita
    public void addGame(Game game) {
        games.put(game.getId(), game);

    }

    //verifica la presenza di un account
    public boolean isPresent(String gameId) {
        return games.containsKey(gameId);
    }

    //recupera una partita
    public Game getGame(String gameId) {
        return games.get(gameId);
    }

}