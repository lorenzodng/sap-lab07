package ttt_api_gateway.application;

import common.exagonal.OutBoundPort;
import ttt_api_gateway.domain.Account;
import ttt_api_gateway.domain.AccountRef;

//interfaccia che contiene tutti i metodi che l'api gateway può richiamare per interagire il servizio degli account
@OutBoundPort
public interface AccountService  {

    //registra un utente al servizio
    AccountRef registerUser(String userName, String password) throws AccountAlreadyPresentException, ServiceNotAvailableException;

    //recupera le informazioni di un account
    Account getAccountInfo(String userName) throws AccountNotFoundException, ServiceNotAvailableException;
}
