package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;
import com.example.paetz.yacguide.utils.ParserUtils;

import org.json.JSONException;

import java.util.LinkedList;

public abstract class JSONWebParser implements NetworkListener {

    protected static final String baseUrl = "http://db-sandsteinklettern.gipfelbuch.de/";

    protected AppDatabase db;
    protected UpdateListener listener;
    protected LinkedList<NetworkRequest> networkRequests;
    private boolean _success;
    private int _processedRequestsCount;

    public JSONWebParser(AppDatabase db, UpdateListener listener) {
        this.db = db;
        this.listener = listener;
        networkRequests = new LinkedList<NetworkRequest>(); // filled in subclasses
        _success = true;
    }

    @Override
    public void onNetworkTaskResolved(int requestId, String result) {
        if (_success) {
            try {
                if (result == null) {
                    throw new JSONException("");
                } else if (result.equalsIgnoreCase("null")) {
                    // sandsteinklettern.de returns "null" string if there are no elements
                    throw new IllegalArgumentException("");
                }
                // remove HTML-encoded characters
                result = ParserUtils.resolveToUtf8(result);
                parseData(requestId, result);
            } catch (JSONException e) {
                _success = false;
            } catch (IllegalArgumentException e) {}
        }
        if (++_processedRequestsCount == networkRequests.size()) {
            listener.onEvent(_success);
            _success = true;
        }
    }

    public void fetchData() {
        _processedRequestsCount = 0;
        for (NetworkRequest request : networkRequests) {
            (new NetworkTask(request.requestId, this)).execute(request.url);
        }
    }

    protected abstract void parseData(int requestId, String json) throws JSONException;

}
