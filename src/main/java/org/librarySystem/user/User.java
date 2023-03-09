package org.librarySystem.user;

public class User {
    private int id;
    private String login;
    private String password;

    public User() {

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(int id, String login, String password) {
        setId(id);
        setLogin(login);
        setPassword(password);
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
