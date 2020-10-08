package de.greenlood.alsatian.webstorm.plugin.rest;

public class AlsatianTestMethod {
    private final String key;
    private final String description;
    private final boolean ignored;
    private final boolean focussed;
    private final long timeout;

    public AlsatianTestMethod(String key, String description, boolean ignored, boolean focussed, long timeout) {

        this.key = key;
        this.description = description;
        this.ignored = ignored;
        this.focussed = focussed;
        this.timeout = timeout;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public boolean isFocussed() {
        return focussed;
    }

    public long getTimeout() {
        return timeout;
    }
}
