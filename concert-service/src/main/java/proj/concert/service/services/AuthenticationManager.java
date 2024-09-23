package proj.concert.service.services;

import java.util.HashMap;

import proj.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;

public class AuthenticationManager {
    private static AuthenticationManager instance;
    private HashMap<String, Long> authTokens = new HashMap<>(); // token:user_id

    private AuthenticationManager(){}

    public static AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    public void addAuthToken(String authToken, User user) {
        authTokens.put(authToken, user.getId());
    }

    public boolean isUserLoggedIn(String authToken) {
        if (authToken == null) {
            return false;
        }
        return authTokens.containsKey(authToken);
    }

    public User getUserFromToken(String authToken) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        return em.find(User.class, authTokens.get(authToken));
    }
}
