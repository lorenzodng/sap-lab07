package ttt_api_gateway.application;

import common.exagonal.InBoundPort;
import ttt_api_gateway.domain.Account;
import ttt_api_gateway.domain.AccountRef;

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
	AccountRef registerUser(String userName, String password) throws AccountAlreadyPresentException, ServiceNotAvailableException;

	/**
     * 
     * Get account info.
     * 
     * @param userName
     * @return
     * @throws AccountNotFoundException
     */
	Account getAccountInfo(String userName) throws AccountNotFoundException, ServiceNotAvailableException;
		
	
	/**
	 * 
	 * Check password validity
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws AccountNotFoundException
	 */
	boolean isValidPassword(String userName, String password) throws AccountNotFoundException, ServiceNotAvailableException;

    
}
