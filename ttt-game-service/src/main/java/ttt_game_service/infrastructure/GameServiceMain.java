package ttt_game_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_game_service.application.*;

public class GameServiceMain {

    static final int GAME_SERVICE_PORT = 9002; //porta sul quale il server ascolta le richiesta http

    public static void main(String[] args) {

        var service = new GameServiceImpl(); //crea un'istanza del servizio di gioco
        service.bindGameRepository(new InMemoryGameRepository()); //crea un repository delle partite e lo collega al servizio di gioco
        var vertx = Vertx.vertx(); //crea un'istanza vertx per gestire le richieste http
        var server = new GameServiceController(service, GAME_SERVICE_PORT); //crea un'istanza del controller
        vertx.deployVerticle(server); //avvia il server sulla porta specificata (esegue il metodo "start" del controller"
    }

}