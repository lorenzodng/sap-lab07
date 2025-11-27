package ttt_account_service.application;

import common.exagonal.InBoundPort;
import ttt_account_service.domain.Account;

//interfaccia che contiene tutti i metodi che il client può richiamare per interagire il servizio degli account
@InBoundPort
public interface AccountService  {

    //registra un utente al servizio
    Account registerUser(String userName, String password) throws AccountAlreadyPresentException;

    //recupera un account
    Account getAccountInfo(String userName) throws AccountNotFoundException;

    //verifica la validità della password
    boolean isValidPassword(String userName, String password) throws AccountNotFoundException;
}

