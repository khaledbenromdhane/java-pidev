package com.pidev;

import com.pidev.entities.User;

public class SessionManager {

    private static SessionManager instance;
    private User userConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getUserConnecte() { return userConnecte; }
    public void setUserConnecte(User user) { this.userConnecte = user; }
    public void deconnecter() { this.userConnecte = null; }
}
