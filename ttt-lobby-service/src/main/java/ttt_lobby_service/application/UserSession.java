package ttt_lobby_service.application;

import ttt_lobby_service.domain.UserId;

//sessione dell'utente (ancora non giocatore)
public class UserSession {

    private String sessionId;
    private UserId userId;
    private LobbyService lobbyService;

    public UserSession(String sessionId, UserId userId, LobbyService lobby) {
        this.userId = userId;
        this.lobbyService = lobby;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserId getUserId() {
        return userId;
    }
}
