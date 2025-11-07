package ttt_account_service.infrastructure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import common.exagonal.Adapter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import ttt_account_service.application.AccountNotFoundException;
import ttt_account_service.application.AccountRepository;
import ttt_account_service.domain.Account;

/**
 * 
 * A simple file-based implementation of the AccountRepository.
 * 
 */
@Adapter
public class SimpleFileBasedAccountRepository implements AccountRepository {
	static Logger logger = Logger.getLogger("[MyDB]");

	/* db file */
	static final String DB_USERS = "users.json";
	
	private HashMap<String, Account> userAccounts;
	
	public SimpleFileBasedAccountRepository() {
		userAccounts = new HashMap<>();
		initFromDB();
	}
	
	public void addAccount(Account account) {
		userAccounts.put(account.getId(), account);
		saveOnDB();
	}
	
	
	public boolean isPresent(String userName) {
		return userAccounts.containsKey(userName);
	}
	
	@Override
	public Account getAccount(String userName) throws AccountNotFoundException {
		return userAccounts.get(userName);
	}

	private void initFromDB() {
		try {
			var usersDB = new BufferedReader(new FileReader(DB_USERS));
			var sb = new StringBuffer();
			while (usersDB.ready()) {
				sb.append(usersDB.readLine()+"\n");
			}
			usersDB.close();
			var array = new JsonArray(sb.toString());
			for (int i = 0; i < array.size(); i++) {
				var user = array.getJsonObject(i);
				Account acc = new Account(user.getString("userName"), user.getString("password"), user.getLong("whenCreated"));
				userAccounts.put(acc.getId(), acc);
			}
		} catch (Exception ex) {
			//	ex.printStackTrace();
			logger.info("DB not found, creating an empty one.");
			saveOnDB();
		}
	}
	
	private void saveOnDB() {
		try {
			JsonArray list = new JsonArray();
			for (Account ac: userAccounts.values()) {
				var obj = new JsonObject();
				obj.put("userName", ac.getUserName());
				obj.put("password", ac.getPassword());
				obj.put("whenCreated", ac.getWhenCreated());
				list.add(obj);
			}
			var usersDB = new FileWriter(DB_USERS);
			usersDB.append(list.encodePrettily());
			usersDB.flush();
			usersDB.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}

	@Override
	public boolean isValid(String userName, String password) {
		return (userAccounts.containsKey(userName) && userAccounts.get(userName).getPassword().equals(password));
	}
	
}
