package ttt_account_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_account_service.application.AccountServiceImpl;

public class AccountServiceMain {

    static final int ACCOUNT_SERVICE_PORT = 9000; //porta sul quale il server ascolta le richiesta http

    public static void main(String[] args) {

        var service = new AccountServiceImpl(); //crea un'istanza del servizio account
        service.bindAccountRepository(new InMemoryAccountRepository()); //crea un repository degli account utente e lo collega al servizio account
        var vertx = Vertx.vertx(); //crea un'istanza vertx per gestire le richieste http
        var server = new AccountServiceController(service, ACCOUNT_SERVICE_PORT); //crea un'istanza del controller
        vertx.deployVerticle(server); //avvia il server sulla porta specificata (esegue il metodo "start" del controller"
    }
}

