package com.mobilejazz.app.model;

import com.mobilejazz.library.BaseObject;

/**
 * Created by Jose Luis on 26/03/15.
 */
public class User extends BaseObject {

    private int idUser;
    private String name;
    private String surname;

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
