package com.sambhar.sambharappreport.rest;

import android.arch.lifecycle.LiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;

public class LiveDataCallAdapter<R> implements CallAdapter<R, LiveData<ApiResponse<R>>> {
    private final Gson mGson;
    private final Type mResponseType;

    public LiveDataCallAdapter(Type type, Gson gson) {
        this.mResponseType = type;
        this.mGson = gson;
    }

    public Type responseType() {
        return TypeToken.get(ResponseBody.class).getType();
    }

    public LiveData<ApiResponse<R>> adapt(final Call<R> call) {
        return new LiveData<ApiResponse<R>>() {
            AtomicBoolean mStarted = new AtomicBoolean(false);

            /* Access modifiers changed, original: protected */
            public void onActive() {
                super.onActive();
                if (this.mStarted.compareAndSet(false, true)) {
                    final ApiResponse apiResponse = new ApiResponse();
                    call.enqueue(new Callback<R>() {
                        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x004a */
                        /* JADX WARNING: Can't wrap try/catch for region: R(6:3|4|5|6|7|8) */
                        public void onResponse(retrofit2.Call<R> r4, retrofit2.Response<R> r5) {
                            /*
                            r3 = this;
                            r4 = r5.isSuccessful();
                            r0 = 0;
                            if (r4 == 0) goto L_0x0068;
                        L_0x0007:
                            r4 = r0;
                            r1 = 1;
                            r4.setSuccessful(r1);
                            r4 = r5.body();	 Catch:{ Exception -> 0x0050 }
                            r4 = (okhttp3.ResponseBody) r4;	 Catch:{ Exception -> 0x0050 }
                            r5 = r4.string();	 Catch:{ Exception -> 0x0050 }
                            r2 = "Response";
                            r4 = r4.string();	 Catch:{ Exception -> 0x0050 }
                            android.util.Log.i(r2, r4);	 Catch:{ Exception -> 0x0050 }
                            r4 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.AnonymousClass1.this;	 Catch:{ Exception -> 0x0050 }
                            r4 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.this;	 Catch:{ Exception -> 0x0050 }
                            r4 = r4.mGson;	 Catch:{ Exception -> 0x0050 }
                            r2 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.AnonymousClass1.this;	 Catch:{ Exception -> 0x0050 }
                            r2 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.this;	 Catch:{ Exception -> 0x0050 }
                            r2 = r2.mResponseType;	 Catch:{ Exception -> 0x0050 }
                            r4 = r4.fromJson(r5, r2);	 Catch:{ Exception -> 0x0050 }
                            r2 = r0;	 Catch:{ Exception -> 0x0050 }
                            r2.setBody(r4);	 Catch:{ Exception -> 0x0050 }
                            r4 = new org.json.JSONObject;	 Catch:{ Exception -> 0x004a }
                            r4.<init>(r5);	 Catch:{ Exception -> 0x004a }
                            r5 = "status";
                            r4 = r4.getInt(r5);	 Catch:{ Exception -> 0x004a }
                            r5 = r0;	 Catch:{ Exception -> 0x004a }
                            r5.setStatusCode(r4);	 Catch:{ Exception -> 0x004a }
                            goto L_0x0059;
                        L_0x004a:
                            r4 = r0;	 Catch:{ Exception -> 0x0050 }
                            r4.setStatusCode(r1);	 Catch:{ Exception -> 0x0050 }
                            goto L_0x0059;
                        L_0x0050:
                            r4 = move-exception;
                            r5 = r0;
                            r5.setSuccessful(r0);
                            r4.printStackTrace();
                        L_0x0059:
                            r4 = r0;
                            r5 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
                            r4.setNetworkCode(r5);
                            r4 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.AnonymousClass1.this;
                            r5 = r0;
                            r4.postValue(r5);
                            goto L_0x00a3;
                        L_0x0068:
                            r4 = r0;
                            r4.setSuccessful(r0);
                            r4 = new org.json.JSONObject;	 Catch:{ Exception -> 0x0086 }
                            r0 = r5.errorBody();	 Catch:{ Exception -> 0x0086 }
                            r0 = r0.string();	 Catch:{ Exception -> 0x0086 }
                            r4.<init>(r0);	 Catch:{ Exception -> 0x0086 }
                            r0 = "message";
                            r4 = r4.getString(r0);	 Catch:{ Exception -> 0x0086 }
                            r0 = r0;	 Catch:{ Exception -> 0x0086 }
                            r0.setErrorMessage(r4);	 Catch:{ Exception -> 0x0086 }
                            goto L_0x0093;
                        L_0x0086:
                            r4 = move-exception;
                            r0 = r0;
                            r1 = r5.message();
                            r0.setErrorMessage(r1);
                            r4.printStackTrace();
                        L_0x0093:
                            r4 = r0;
                            r5 = r5.code();
                            r4.setNetworkCode(r5);
                            r4 = com.sambhar.sambharappreport.rest.LiveDataCallAdapter.AnonymousClass1.this;
                            r5 = r0;
                            r4.postValue(r5);
                        L_0x00a3:
                            return;
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.sambhar.sambharappreport.rest.LiveDataCallAdapter$1$AnonymousClass1.onResponse(retrofit2.Call, retrofit2.Response):void");
                        }

                        public void onFailure(Call<R> call, Throwable th) {
                            apiResponse.setSuccessful(false);
                            apiResponse.setErrorMessage(th.toString());
                            AnonymousClass1.this.postValue(apiResponse);
                        }
                    });
                }
            }
        };
    }
}
