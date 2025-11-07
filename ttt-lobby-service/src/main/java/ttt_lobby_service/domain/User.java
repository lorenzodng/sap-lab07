package ttt_lobby_service.domain;

import common.ddd.Entity;

/**
 * 
 * Modelling a user account. 
 * 
 */
public class User implements Entity<String> {
	
	private String userName; /* this is the id */
	private String password;
	
	public User(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getId() {
		return userName;
	}
	
}
