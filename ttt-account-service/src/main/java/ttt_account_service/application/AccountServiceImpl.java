package ttt_account_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;
import ttt_account_service.domain.Account;

//servizio applicativo che gestisce gli account
public class AccountServiceImpl implements AccountService {

    static Logger logger = Logger.getLogger("[TTT Account Service]");
    private AccountRepository accountRepository; //repository degli account

    //registra un utente al servizio
    public Account registerUser(String userName, String password) throws AccountAlreadyPresentException {
        logger.log(Level.INFO, "Register User: " + userName + " " + password);
        if (accountRepository.isPresent(userName)) { //se l'utente esiste già
            throw new AccountAlreadyPresentException(); //lancia un'eccezione
        }
        var account = new Account(userName, password); //crea l'account
        accountRepository.addAccount(account); //lo aggiunge
        return account; //restituisce l'account creato
    }

    //recupera un account
    public Account getAccountInfo(String userName) throws AccountNotFoundException {
        logger.log(Level.INFO, "Get account info: " + userName);
        if (!accountRepository.isPresent(userName)) { //se l'utente non esiste
            throw new AccountNotFoundException(); //lancia un'eccezione
        }
        return accountRepository.getAccount(userName); //recupera un account
    }

    //verifica la validità della password
    @Override
    public boolean isValidPassword(String userName, String password) throws AccountNotFoundException {
        logger.log(Level.INFO, "IsValid password " + userName + " - " + password);
        if (!accountRepository.isPresent(userName)) { //se l'utente non esiste
            throw new AccountNotFoundException(); //lancia un'eccezione
        }
        return accountRepository.getAccount(userName).getPassword().equals(password); //verifica se la password inserita corrisponde a quella associata all'account
    }

    //definisce un repository per gli account
    public void bindAccountRepository(AccountRepository repo) {
        this.accountRepository = repo;
    }
}
