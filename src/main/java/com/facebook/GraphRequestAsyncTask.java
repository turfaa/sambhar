package com.facebook;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.facebook.internal.Utility;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;

public class GraphRequestAsyncTask extends AsyncTask<Void, Void, List<GraphResponse>> {
    private static final String TAG = GraphRequestAsyncTask.class.getCanonicalName();
    private final HttpURLConnection connection;
    private Exception exception;
    private final GraphRequestBatch requests;

    public GraphRequestAsyncTask(GraphRequest... graphRequestArr) {
        this(null, new GraphRequestBatch(graphRequestArr));
    }

    public GraphRequestAsyncTask(Collection<GraphRequest> collection) {
        this(null, new GraphRequestBatch((Collection) collection));
    }

    public GraphRequestAsyncTask(GraphRequestBatch graphRequestBatch) {
        this(null, graphRequestBatch);
    }

    public GraphRequestAsyncTask(HttpURLConnection httpURLConnection, GraphRequest... graphRequestArr) {
        this(httpURLConnection, new GraphRequestBatch(graphRequestArr));
    }

    public GraphRequestAsyncTask(HttpURLConnection httpURLConnection, Collection<GraphRequest> collection) {
        this(httpURLConnection, new GraphRequestBatch((Collection) collection));
    }

    public GraphRequestAsyncTask(HttpURLConnection httpURLConnection, GraphRequestBatch graphRequestBatch) {
        this.requests = graphRequestBatch;
        this.connection = httpURLConnection;
    }

    /* Access modifiers changed, original: protected|final */
    public final Exception getException() {
        return this.exception;
    }

    /* Access modifiers changed, original: protected|final */
    public final GraphRequestBatch getRequests() {
        return this.requests;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{RequestAsyncTask: ");
        stringBuilder.append(" connection: ");
        stringBuilder.append(this.connection);
        stringBuilder.append(", requests: ");
        stringBuilder.append(this.requests);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public void onPreExecute() {
        super.onPreExecute();
        if (FacebookSdk.isDebugEnabled()) {
            Utility.logd(TAG, String.format("execute async task: %s", new Object[]{this}));
        }
        if (this.requests.getCallbackHandler() == null) {
            Handler handler;
            if (Thread.currentThread() instanceof HandlerThread) {
                handler = new Handler();
            } else {
                handler = new Handler(Looper.getMainLooper());
            }
            this.requests.setCallbackHandler(handler);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(List<GraphResponse> list) {
        super.onPostExecute(list);
        if (this.exception != null) {
            Utility.logd(TAG, String.format("onPostExecute: exception encountered during request: %s", new Object[]{this.exception.getMessage()}));
        }
    }

    /* Access modifiers changed, original: protected|varargs */
    public List<GraphResponse> doInBackground(Void... voidArr) {
        try {
            if (this.connection == null) {
                return this.requests.executeAndWait();
            }
            return GraphRequest.executeConnectionAndWait(this.connection, this.requests);
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }
}
