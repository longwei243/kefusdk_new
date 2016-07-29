package com.moor.imkf;

import java.io.File;

/**
 * Created by longwei on 16/7/28.
 */
public interface FileMessageDownLoadListener {
    void onSuccess(File file);
    void onFailed();
    void onProgress();
}
