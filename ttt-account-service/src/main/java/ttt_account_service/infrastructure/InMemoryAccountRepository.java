package ttt_account_service.infrastructure;

import java.util.HashMap;
import common.exagonal.Adapter;
import ttt_account_service.application.AccountRepository;
import ttt_account_service.domain.Account;

//implementazione della porta di uscita che collega l'architettura (applicazione) al db degli account
@Adapter
public class InMemoryAccountRepository implements AccountRepository {

    private HashMap<String, Account> userAccounts; //hashmap che associa l'utente all'account

    public InMemoryAccountRepository() {
        userAccounts = new HashMap<>();
    }

    //aggiunge un account
    public void addAccount(Account account) {
        userAccounts.put(account.getId(), account);
    }

    //recupera un account
    @Override
    public Account getAccount(String userName) {
        return userAccounts.get(userName);
    }

    //verifica la presenza di un account
    public boolean isPresent(String userName) {
        return userAccounts.containsKey(userName);
    }
}
