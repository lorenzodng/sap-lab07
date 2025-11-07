package ttt_account_service.application;

import common.ddd.Repository;
import common.exagonal.OutBoundPort;
import ttt_account_service.domain.Account;

/**
 * 
 * Interface of account repository
 * 
 */
@OutBoundPort
public interface AccountRepository extends Repository {

	void addAccount(Account account);
	
	boolean isPresent(String userName);

	Account getAccount(String userName) throws AccountNotFoundException;
	
	boolean isValid(String userName, String password);
}
