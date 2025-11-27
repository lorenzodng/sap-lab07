package ttt_account_service.application;

import common.ddd.Repository;
import common.exagonal.OutBoundPort;
import ttt_account_service.domain.Account;

/*
interfaccia che collega l'architettura (applicazione) al db degli account
contiene tutti i metodi che l'architettura utilizza per interagire con il db degli account
 */
@OutBoundPort
public interface AccountRepository extends Repository {

    //aggiunge un account
    void addAccount(Account account);

    //verifica se un account è presente
    boolean isPresent(String userName);

    //recupera un account
    Account getAccount(String userName) throws AccountNotFoundException;
}
