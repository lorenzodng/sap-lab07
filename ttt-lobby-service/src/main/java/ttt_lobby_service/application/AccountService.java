package ttt_lobby_service.application;

import common.exagonal.OutBoundPort;
import ttt_lobby_service.domain.User;

@OutBoundPort
public interface AccountService  {
	
	/**
	 * 
	 * Check password validity
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws UserNotFoundException
	 */
	boolean isValidPassword(String userName, String password) 
			throws UserNotFoundException, ServiceNotAvailableException;;

    
}
