package com.example.paetz.yacguide.network;

import com.example.paetz.yacguide.UpdateListener;
import com.example.paetz.yacguide.database.AppDatabase;

import org.json.JSONException;
import org.json.JSONObject;

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
                }
                parseData(requestId, result);
            } catch (JSONException e) {
                _success = false;
            }
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

    protected int jsonField2Int(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return Integer.parseInt(jsonObject.getString(fieldName));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected float jsonField2Float(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return Float.parseFloat(jsonObject.getString(fieldName));
        } catch (NumberFormatException e) {
            return 0.f;
        }
    }

    protected char jsonField2Char(JSONObject jsonObject, String fieldName) throws JSONException {
        try {
            return jsonObject.getString(fieldName).charAt(0);
        } catch (IndexOutOfBoundsException e) {
            return ' ';
        }
    }
}