package datacomp.co.nz.datalockandroid.model;

/**
 * Created by jonker on 27/07/14.
 */
public class Event {
    long id;
    String action;
    Long userId;
    long tempUserId;
    String createdAt;
    String updatedAt;

    public Event(long id, String action, Long userId, String createdAt, String updatedAt) {
        this.id = id;
        this.action = action;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Event() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTempUserId() {
        return tempUserId;
    }

    public void setTempUserId(long tempUserId) {
        this.tempUserId = tempUserId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    //{"id":288,"action":"Android Unlock","user_id":1,"temp_user_id":null,"created_at":"2014-07-26T20:32:50.793Z","updated_at":"2014-07-26T20:32:50.793Z"}
}
