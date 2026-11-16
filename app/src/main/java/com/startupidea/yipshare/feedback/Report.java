package com.startupidea.yipshare.feedback;

public final class Report {

    private final String id;
    private final String text;
    private final ReportState state;

    public Report(String id, String text, ReportState state) {
        this.id = id;
        this.text = text;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public ReportState getState() {
        return state;
    }
}
