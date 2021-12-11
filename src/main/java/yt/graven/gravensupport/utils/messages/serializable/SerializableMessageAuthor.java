package yt.graven.gravensupport.utils.messages.serializable;

import com.google.gson.annotations.Expose;

public class SerializableMessageAuthor {

    @Expose public long id = 0L;
    @Expose public String name = "";
    @Expose public String avatarUrl = "";

    public SerializableMessageAuthor() {
    }

    public SerializableMessageAuthor(long id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
