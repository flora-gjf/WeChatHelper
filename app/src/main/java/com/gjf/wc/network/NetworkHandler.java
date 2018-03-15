package com.gjf.wc.network;

/**
 * Created by guojunfu on 18/3/15.
 */

public interface NetworkHandler<T> {
    void onSuccess(T var1);

    void onFailure(int var1);
}
