package ttt_lobby_service.application;

import common.exagonal.OutBoundPort;

//interfaccia che contiene tutti i metodi che la lobby (ambiente pre-partita) può richiamare per interagire con il servizio degli account
@OutBoundPort
public interface AccountService  {

    //verifica la validità della password
    boolean isValidPassword(String userName, String password) throws UserNotFoundException, ServiceNotAvailableException;;

}
