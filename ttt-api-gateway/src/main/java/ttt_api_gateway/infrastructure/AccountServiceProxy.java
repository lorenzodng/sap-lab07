package ttt_api_gateway.infrastructure;

import common.exagonal.Adapter;
import java.net.http.HttpResponse;
import io.vertx.core.json.JsonObject;
import ttt_api_gateway.application.AccountAlreadyPresentException;
import ttt_api_gateway.application.AccountNotFoundException;
import ttt_api_gateway.application.AccountService;
import ttt_api_gateway.application.ServiceNotAvailableException;
import ttt_api_gateway.domain.Account;
import ttt_api_gateway.domain.AccountRef;

@Adapter
public class AccountServiceProxy extends HTTPSyncBaseProxy implements AccountService {

	private String serviceAddress;

	public AccountServiceProxy(String serviceAPIEndpoint) {
		this.serviceAddress = serviceAPIEndpoint;
	}

	@Override
	public boolean isValidPassword(String userName, String password)
			throws AccountNotFoundException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("password", password);

			HttpResponse<String> response = doPost(serviceAddress + "/api/v1/accounts/" + userName + "/check-pwd", body);

			System.out.println("Response Code: " + response.statusCode());

			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());

				var res = json.getString("result");
				if (res.equals("valid-password")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}

		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public AccountRef registerUser(String userName, String password)
			throws AccountAlreadyPresentException, ServiceNotAvailableException {
		try {
			JsonObject body = new JsonObject();
			body.put("password", password);
			body.put("userName", userName);

			HttpResponse<String> response = doPost( serviceAddress + "/api/v1/accounts", body);
			
			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());
				return new AccountRef(userName, json.getString("accountLink"));
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}

	@Override
	public Account getAccountInfo(String userName) throws AccountNotFoundException, ServiceNotAvailableException {
		try {
			HttpResponse<String> response = doGet( serviceAddress + "/api/v1/accounts/" + userName);
			
			if (response.statusCode() == 200) {
				JsonObject json = new JsonObject(response.body());
				JsonObject obj = json.getJsonObject("accountInfo");
				return new Account(obj.getString("userName"), obj.getString("password"), obj.getLong("whenCreated"));
			} else {
				throw new ServiceNotAvailableException();
			}
		} catch (Exception ex) {
			throw new ServiceNotAvailableException();
		}
	}
	

}
