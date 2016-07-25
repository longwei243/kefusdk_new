package com.moor.imkf;

import com.moor.imkf.model.entity.Peer;

import java.util.List;

/**
 * Created by longwei on 2016/3/8.
 */
public interface GetPeersListener {

    void onSuccess(List<Peer> peers);
    void onFailed();
}
