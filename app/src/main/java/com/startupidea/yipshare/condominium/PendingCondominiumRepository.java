package com.startupidea.yipshare.condominium;

// TODO: send the request and its PDFs to the API; the global administrator approves it on the web.
// It must run off the main thread and throw DatabaseUnavailable when the API cannot be reached.
public class PendingCondominiumRepository implements CondominiumRepository {

    @Override
    public void createCondominiumRequest(CondominiumPayload payload) {
    }
}
