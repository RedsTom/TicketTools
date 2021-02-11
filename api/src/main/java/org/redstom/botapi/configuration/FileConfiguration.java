package org.redstom.botapi.configuration;

import com.google.gson.JsonObject;

import java.io.IOException;

public interface FileConfiguration {

    void update() throws IOException;

    void save() throws IOException;

    JsonObject getValues();

}
