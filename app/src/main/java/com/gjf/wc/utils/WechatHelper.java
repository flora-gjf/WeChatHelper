package com.gjf.wc.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.gjf.wc.R;
import com.gjf.wc.model.WXAccessTokenInfo;
import com.gjf.wc.model.WXErrorInfo;
import com.gjf.wc.model.WXUserInfo;
import com.gjf.wc.network.NetworkHandler;
import com.gjf.wc.network.NetworkHttpParam;
import com.gjf.wc.network.OkHttpHelper;
import com.gjf.wc.observable.WechatObservable;
import com.gjf.wc.observable.WechatObserver;
import com.gjf.wc.share.ShareParams;
import com.gjf.wc.share.ShareType;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by guojunfu on 18/3/15.
 */

public class WechatHelper {
    private static final String TAG = "WechatManager";

    private final String GET_REQUEST_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private final String GET_EXPIRE_TOKEN_URL = "https://api.weixin.qq.com/sns/aaswuth";
    private final String GET_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";
    private final String GET_REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token";

    public static final int WECHAT_CHECK_SUCCESS = 166;
    public static final int WECHAT_CHECK_FAILURE = 167;

    private static IWXAPI api;

    private static WechatHelper instance;

    private static WechatObservable mWechatObservable;

    private WXErrorInfo mWXErrorInfo;

    public void addWechatObserver(WechatObserver observer) {
        mWechatObservable.addObserver(observer);
    }

    public void removeWechatObserver(WechatObserver observer) {
        mWechatObservable.deleteObserver(observer);
    }

    public void removeAllWechatObserver() {
        mWechatObservable.deleteObservers();
    }

    public void sendUserConfirmMessage(int code) {
        mWechatObservable.sendStateChange(code);
    }

    private WechatHelper() {
        api = WXAPIFactory.createWXAPI(GlobalConfig.getAppContext(), UserConstants.WECHAT_APPIDS, true);
        api.registerApp(UserConstants.WECHAT_APPIDS);
        mWechatObservable = new WechatObservable();
    }

    public static WechatHelper getInstance() {
        if (instance == null) {
            synchronized (WechatHelper.class) {
                if (instance == null) {
                    instance = new WechatHelper();
                }
            }
        }

        return instance;
    }

    public IWXAPI getApi() {
        return api;
    }

    public void authorizeByWechat() {
        Log.i(TAG, "authorizeByWechat: ");

        if (!isAvailable()) {
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
            return;
        }

        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "app_wechat";
        api.sendReq(req);
    }

    public void checkAccessToken(String code) {
        // 从手机本地获取存储的授权口令信息，判断是否存在access_token，不存在请求获取，存在就判断是否过期
        String accessToken = WechatInfoSPHelper.getWechatAccessToken();
        String openid = WechatInfoSPHelper.getWechatOpenid();

        if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(openid)) {
            // 有access_token，判断是否过期有效
            isExpireAccessToken(accessToken, openid);
        } else {
            getTokenFromCode(code);
        }
    }

    private void isExpireAccessToken(final String accessToken, final String openid) {
        Log.i(TAG, "isExpireAccessToken: ");
        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(openid)) {
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
            return;
        }

        // 使用网络库发起请求
        NetworkHttpParam params = new NetworkHttpParam();
        params.put("access_token", accessToken);
        params.put("openid", openid);

        OkHttpHelper.getInstance().requestStringGet(GET_EXPIRE_TOKEN_URL, params, new WechatDataHandler(false) {

            @Override
            public void onWechatSuccess(String response) {
                super.onWechatSuccess(response);

                if (validateSuccess(response)) {
                    // accessToken没有过期，获取用户信息
                    getUserInfo(accessToken, openid);
                } else {
                    // 过期了，使用refresh_token来刷新accesstoken
                    refreshAccessToken();
                }
            }
        });
    }

    private void refreshAccessToken() {
        Log.i(TAG, "refreshAccessToken: ");
        // 从本地获取存储的refresh_token
        final String refreshToken = WechatInfoSPHelper.getWechatRefreshToken();

        if (refreshToken == null) {
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
            return;
        }

        // 发起网络请求 刷新token
        NetworkHttpParam params = new NetworkHttpParam();
        params.put("appid", UserConstants.WECHAT_APPIDS);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);

        OkHttpHelper.getInstance().requestStringGet(GET_REFRESH_TOKEN_URL, params, new WechatDataHandler(true) {

            @Override
            public void onWechatDataSuccess(String response) {
                super.onWechatDataSuccess(response);

                WXAccessTokenInfo tokenInfo = JsonFactory.fromJson(response, WXAccessTokenInfo.class);

                if (tokenInfo == null) {
                    mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                } else {
                    WechatInfoSPHelper.saveWechatAccessInfoToSP(tokenInfo);
                    getUserInfo(tokenInfo.access_token, tokenInfo.openid);
                }
            }
        });
    }

    /**
     * 使用code获取微信登陆token凭证
     */
    private void getTokenFromCode(String code) {
        Log.i(TAG, "getTokenFromCode: ");
        if (TextUtils.isEmpty(code)) {
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
            return;
        }

        // 发起网络请求 刷新token
        NetworkHttpParam params = new NetworkHttpParam();
        params.put("appid", UserConstants.WECHAT_APPIDS);
        params.put("secret", UserConstants.WECHAT_SECRET);
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        OkHttpHelper.getInstance().requestStringGet(GET_REQUEST_ACCESS_TOKEN_URL, params, new WechatDataHandler(true) {

            @Override
            public void onWechatDataSuccess(String response) {
                super.onWechatDataSuccess(response);

                WXAccessTokenInfo tokenInfo = JsonFactory.fromJson(response, WXAccessTokenInfo.class);

                if (tokenInfo == null) {
                    mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                } else {
                    WechatInfoSPHelper.saveWechatAccessInfoToSP(tokenInfo);

                    getUserInfo(tokenInfo.access_token, tokenInfo.openid);
                }
            }
        });
    }

    // 使用access_token获取用户信息
    private void getUserInfo(String access_token, String openid) {
        Log.i(TAG, "getUserInfo: ");

        if (TextUtils.isEmpty(access_token) || TextUtils.isEmpty(openid)) {
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
            return;
        }

        // 发起网络请求 刷新token
        NetworkHttpParam params = new NetworkHttpParam();
        params.put("access_token", access_token);
        params.put("lang", "zh_CN");
        params.put("openid", openid);

        OkHttpHelper.getInstance().requestStringGet(GET_USER_INFO_URL, params, new WechatDataHandler(true) {

            @Override
            public void onWechatDataSuccess(String response) {
                super.onWechatDataSuccess(response);

                WXUserInfo userInfo = JsonFactory.fromJson(response, WXUserInfo.class);

                if (userInfo == null) {
                    mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                } else {
                    WechatInfoSPHelper.saveWechatUserInfoToSP(userInfo);
                    mWechatObservable.sendStateChange(WECHAT_CHECK_SUCCESS);
                }
            }
        });
    }

    private boolean validateSuccess(String response) {
        Log.i(TAG, "validateSuccess: " + response);

        if (response.contains("errcode") && response.contains("errmsg")) {
            int code = 66666;

            try {
                JSONObject obj = new JSONObject(response);
                code = obj.getInt("errcode");

                if (code != 66666) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return true;
            }
        }

        return true;
    }

    private class LeWechatHandler implements NetworkHandler<Object> {

        @Override
        public void onSuccess(Object response) {
            Log.i(TAG, "WechatVolleyHandler onSuccess: " + response);
            String responseStr = (String) response;

            if (TextUtils.isEmpty(responseStr)) {
                mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                return;
            }

            onWechatSuccess(responseStr);
        }

        @Override
        public void onFailure(int errorCode) {
            Log.i(TAG, "WechatVolleyHandler onFailure: " + errorCode);
            mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
        }

        public void onWechatSuccess(String response) {

        }
    }

    /**
     * 增加异常判断
     */
    private class WechatDataHandler extends LeWechatHandler {

        // 微信校验token接口:  校验不成功时返回数据格式与网络失败格式相同
        private boolean mNeedCheckError = false;

        WechatDataHandler(boolean needCheckError) {
            mNeedCheckError = needCheckError;
        }

        @Override
        public void onWechatSuccess(String response) {
            super.onWechatSuccess(response);

            if (mNeedCheckError && !validateSuccess(response)) {
                mWXErrorInfo = JsonFactory.fromJson(response, WXErrorInfo.class);

                if (mWXErrorInfo == null) {
                    mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                } else {
                    mWechatObservable.sendStateChange(WECHAT_CHECK_FAILURE);
                }

                return;
            }

            onWechatDataSuccess(response);
        }

        public void onWechatDataSuccess(String response) {

        }
    }

    ///////////////// 微信分享 //////////////////////

    /**
     * 微信对话分享
     */
    public void shareWechat(ShareParams params, SucceedAndFailedHandler shareListener) {
        switch (params.getShareType()) {
            case ShareType.LOCAL_IMAGE:
                shareLocalImage(params, shareListener);
                break;
            case ShareType.NETWORK_IMAGE:
                shareNetworkImage(params, shareListener);
                break;
            case ShareType.LINK:
                shareLink(params, shareListener);
                break;
            default:
                shareListener.onFailure(-1);
        }
    }

    /**
     * 微信朋友圈分享
     */
    public void shareWechatMoments(ShareParams params, SucceedAndFailedHandler shareListener) {
        switch (params.getShareType()) {
            case ShareType.LOCAL_IMAGE:
                shareMomentsLocalImage(params, shareListener);
                break;
            case ShareType.NETWORK_IMAGE:
                shareMomentsNetworkImage(params, shareListener);
                break;
            case ShareType.LINK:
                shareMomentsLink(params, shareListener);
                break;
            default:
                shareListener.onFailure(-1);
        }
    }

    /**
     * 微信对话分享 本地图片
     */
    private void shareLocalImage(ShareParams params, SucceedAndFailedHandler shareListener) {
        Bitmap bmp = params.getLocalBitmap();

        if (bmp == null) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        wechatShareImage(shareListener, bmp);
    }

    /**
     * 微信对话分享 网络图片
     */
    private void shareNetworkImage(ShareParams params, final SucceedAndFailedHandler shareListener) {
        String shareImage = params.getNetworkImageUrl();

        if (TextUtils.isEmpty(shareImage)) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        NetworkHandler<Bitmap> imgHandler = new NetworkHandler<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                wechatShareImage(shareListener, bitmap);
            }

            @Override
            public void onFailure(int errorCode) {
                if (shareListener != null) {
                    shareListener.onFailure(-1);
                }
            }
        };

        OkHttpHelper.getInstance().requestBitmapFromUrl(shareImage, imgHandler);
    }

    /**
     * 微信对话分享 在线链接
     */
    private void shareLink(final ShareParams params, final SucceedAndFailedHandler shareListener) {
        if (TextUtils.isEmpty(params.getLinkUrl())) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        // 缩略图
        String thumbUrl = params.getNetworkImageUrl();

        if (TextUtils.isEmpty(thumbUrl)) {
            wechatShareLink(getDefaultBitmap(), params, shareListener);
        } else {
            NetworkHandler<Bitmap> imgHandler = new NetworkHandler<Bitmap>() {
                @Override
                public void onSuccess(Bitmap thumbBitmap) {
                    wechatShareLink(thumbBitmap, params, shareListener);
                }

                @Override
                public void onFailure(int errorCode) {
                    wechatShareLink(getDefaultBitmap(), params, shareListener);
                }
            };

            OkHttpHelper.getInstance().requestBitmapFromUrl(thumbUrl, imgHandler);
        }
    }

    /**
     * 微信朋友圈分享 本地图片
     */
    private void shareMomentsLocalImage(ShareParams params, SucceedAndFailedHandler shareListener) {
        Bitmap bmp = params.getLocalBitmap();

        if (bmp == null) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        wechatMomentsShareImage(bmp, shareListener);
    }

    /**
     * 微信朋友圈分享 网路图片
     */
    private void shareMomentsNetworkImage(ShareParams params, final SucceedAndFailedHandler shareListener) {
        String shareImage = params.getNetworkImageUrl();

        if (TextUtils.isEmpty(shareImage)) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        NetworkHandler<Bitmap> imgHandler = new NetworkHandler<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                wechatMomentsShareImage(bitmap, shareListener);
            }

            @Override
            public void onFailure(int errorCode) {
                if (shareListener != null) {
                    shareListener.onFailure(-1);
                }
            }
        };

        OkHttpHelper.getInstance().requestBitmapFromUrl(shareImage, imgHandler);
    }

    /**
     * 微信朋友圈分享 在线链接
     */
    private void shareMomentsLink(final ShareParams params, final SucceedAndFailedHandler shareListener) {
        if (TextUtils.isEmpty(params.getLinkUrl())) {
            if (shareListener != null) {
                shareListener.onFailure(-1);
            }
            return;
        }

        // 缩略图
        String thumbUrl = params.getNetworkImageUrl();

        if (TextUtils.isEmpty(thumbUrl)) {
            wechatShareLink(getDefaultBitmap(), params, shareListener);
        } else {
            NetworkHandler<Bitmap> imgHandler = new NetworkHandler<Bitmap>() {
                @Override
                public void onSuccess(Bitmap thumbBitmap) {
                    wechatMomentsShareLink(thumbBitmap, params, shareListener);
                }

                @Override
                public void onFailure(int errorCode) {
                    wechatShareLink(getDefaultBitmap(), params, shareListener);
                }
            };

            OkHttpHelper.getInstance().requestBitmapFromUrl(thumbUrl, imgHandler);
        }
    }

    private void wechatShareLink(Bitmap bmp, ShareParams params, SucceedAndFailedHandler handler) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        WXWebpageObject webPageObject = new WXWebpageObject();

        Bitmap thumbBit = Bitmap.createScaledBitmap(bmp, 100, 100, true);

        webPageObject.webpageUrl = params.getLinkUrl();
        WXMediaMessage msg = new WXMediaMessage(webPageObject);
        req.transaction = buildTransaction("webpage");
        msg.title = params.getTitle();
        msg.description = params.getText();
        msg.thumbData = ImageUtil.parseBitmapToBytes(thumbBit);

        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        api.sendReq(req);
    }

    private void wechatShareImage(SucceedAndFailedHandler handler, Bitmap bmp) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        WXImageObject imageObject = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage(imageObject);

        msg.mediaObject = imageObject;

        Bitmap thumbBit = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        msg.thumbData = ImageUtil.parseBitmapToBytes(thumbBit);

        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        api.sendReq(req);
    }

    private void wechatMomentsShareLink(Bitmap bmp, ShareParams params, SucceedAndFailedHandler handler) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        WXWebpageObject webPageObject = new WXWebpageObject();
        webPageObject.webpageUrl = params.getLinkUrl();
        WXMediaMessage msg = new WXMediaMessage(webPageObject);

        Bitmap thumbBit = Bitmap.createScaledBitmap(bmp, 100, 100, true);

        req.transaction = buildTransaction("webpage");
        msg.title = params.getTitle();
        msg.description = params.getText();
        msg.thumbData = ImageUtil.parseBitmapToBytes(thumbBit);
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;

        api.sendReq(req);
    }

    private void wechatMomentsShareImage(Bitmap bmp, SucceedAndFailedHandler handler) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();

        WXImageObject imageObject = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage(imageObject);
        msg.mediaObject = imageObject;

        Bitmap thumbBit = Bitmap.createScaledBitmap(bmp, 100, 100, true);

        msg.thumbData = ImageUtil.parseBitmapToBytes(thumbBit);
        req.transaction = buildTransaction("img");

        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;

        api.sendReq(req);
    }

    /**
     * 获取默认缩略图
     */
    private Bitmap getDefaultBitmap() {
        return BitmapFactory.decodeResource(GlobalConfig.getAppContext().getResources(), R.mipmap.ic_launcher);
    }

    private boolean isAvailable() {
        return api.isWXAppInstalled() && api.isWXAppSupportAPI();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}

