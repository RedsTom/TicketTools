package yt.graven.gravensupport.utils;

public class Config {

    private String token;
    private String prefix;

    public Config(String token, String prefix) {
        this.token = token;
        this.prefix = prefix;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }
}
