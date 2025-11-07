package ttt_account_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_account_service.application.AccountServiceImpl;

public class AccountServiceMain {

	static final int ACCOUNT_SERVICE_PORT = 9000;

	public static void main(String[] args) {
		
		var service = new AccountServiceImpl();
		
		service.bindAccountRepository(new InMemoryAccountRepository());
		
		var vertx = Vertx.vertx();
		var server = new AccountServiceController(service, ACCOUNT_SERVICE_PORT);
		vertx.deployVerticle(server);	
	}

}

