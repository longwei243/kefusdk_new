package com.moor.imkf.http;

import java.io.File;

/**
 * Created by longwei on 2016/4/29.
 */
public interface FileDownLoadListener {

    void onSuccess(File file);
    void onFailed();
    void onProgress(int progress);
}
