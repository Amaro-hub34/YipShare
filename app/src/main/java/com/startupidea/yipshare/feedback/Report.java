package com.startupidea.yipshare.feedback;

public final class Report {

    private final String text;
    private final boolean resolved;

    public Report(String text, boolean resolved) {
        this.text = text;
        this.resolved = resolved;
    }

    public String getText() {
        return text;
    }

    public boolean isResolved() {
        return resolved;
    }
}
