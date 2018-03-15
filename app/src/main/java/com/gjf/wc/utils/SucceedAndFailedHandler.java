package com.gjf.wc.utils;

/**
 * Created by guojunfu on 18/3/15.
 */

public interface SucceedAndFailedHandler {
    void onSuccess(Object obj);

    void onFailure(int errorCode);
}
