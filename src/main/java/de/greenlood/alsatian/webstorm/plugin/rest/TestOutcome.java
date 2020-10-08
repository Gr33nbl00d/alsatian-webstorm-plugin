package de.greenlood.alsatian.webstorm.plugin.rest;

public enum TestOutcome {
    Error(0),
    Fail(1),
    Pass(2),
    Skip(3);

    private int integerCode;

    TestOutcome(int integerCode) {

        this.integerCode = integerCode;
    }

    public int toNumber() {
        return integerCode;
    }

    public static TestOutcome fromNumber(int testResultCode) {
        for (TestOutcome value : TestOutcome.values()) {
            if (value.toNumber() == testResultCode)
                return value;
        }

        throw new IllegalStateException("unknown test result code " + testResultCode);
    }
}
