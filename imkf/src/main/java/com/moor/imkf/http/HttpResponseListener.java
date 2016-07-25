package com.moor.imkf.http;

/**
 * Created by longwei on 2016/4/12.
 */
public interface HttpResponseListener {
    void onSuccess(String responseStr);
    void onFailed();
}
