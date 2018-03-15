package com.gjf.wc.observable;

import java.util.Observable;

/**
 * Created by guojunfu on 16/12/8.
 */

public class WechatObservable extends Observable {

    public void sendStateChange(int code) {
        setChanged();
        notifyObservers(code);
    }
}
