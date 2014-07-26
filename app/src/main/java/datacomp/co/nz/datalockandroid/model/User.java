package datacomp.co.nz.datalockandroid.model;

import java.util.Date;

/**
 * Created by jonker on 27/07/14.
 */
public class User {
    long id;
    String email;
    String name;
    boolean admin;
    int pin;
    String phone;
    String created;
    String updated;

    public User() {
    }

    public User(long id, String email, String name, boolean admin, int pin, String phone, String created, String updated) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.admin = admin;
        this.pin = pin;
        this.phone = phone;
        this.created = created;
        this.updated = updated;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
