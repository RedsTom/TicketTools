package me.redstom.tickettools.configuration.exception;

import me.redstom.tickettools.common.exception.BotStartupException;

public class ConfigurationException extends BotStartupException {

    public ConfigurationException(String reason) {
        super(reason);
    }

    public ConfigurationException(String reason, Throwable throwable) {
        super(reason, throwable);
    }
}
