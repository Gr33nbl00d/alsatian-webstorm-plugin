package de.greenlood.alsatian.webstorm.plugin.rest;


public class TestResultRequest {
    private int outcome;
    private String ignoreReason;
    private JSError error;
    private String[] logs;
    private Long executionTime;

    public int getOutcome() {
        return outcome;
    }

    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }

    public String getIgnoreReason() {
        return ignoreReason;
    }

    public void setIgnoreReason(String ignoreReason) {
        this.ignoreReason = ignoreReason;
    }

    public JSError getError() {
        return error;
    }

    public void setError(JSError error) {
        this.error = error;
    }

    public String[] getLogs() {
        return logs;
    }

    public void setLogs(String[] logs) {
        this.logs = logs;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }
}
