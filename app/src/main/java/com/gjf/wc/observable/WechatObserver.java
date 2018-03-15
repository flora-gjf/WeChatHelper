package com.gjf.wc.observable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by guojunfu on 16/12/8.
 */

public abstract class WechatObserver implements Observer {

    @Override
    public void update(Observable observable, Object data) {
       handleStateChange((int)data);
    }

    public abstract void handleStateChange(int data);

}
