package de.greenlood.alsatian.webstorm.plugin.rest;

public class JSError {
    private final String message;
    private final String stack;

    public JSError(String message, String stack) {
        this.message = message;
        this.stack = stack;
    }

    public String getMessage() {
        return message;
    }

    public String getStack() {
        return stack;
    }
}
