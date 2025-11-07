package ttt_account_service.application;

import common.exagonal.InBoundPort;
import ttt_account_service.domain.Account;

/**
 * 
 * Interface of the Game Service at the application layer
 * 
 */
@InBoundPort
public interface AccountService  {

	/**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     * @throws AccountAlreadyPresentException
     */
	Account registerUser(String userName, String password) throws AccountAlreadyPresentException;

	/**
     * 
     * Get account info.
     * 
     * @param userName
     * @return
     * @throws AccountNotFoundException
     */
	Account getAccountInfo(String userName) throws AccountNotFoundException;
		
	
	/**
	 * 
	 * Check password validity
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws AccountNotFoundException
	 */
	boolean isValidPassword(String userName, String password) throws AccountNotFoundException;

    
}
