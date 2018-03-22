package com.gjf.wc.activity;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.gjf.wc.R;
import com.gjf.wc.share.ShareParams;
import com.gjf.wc.share.ShareType;
import com.gjf.wc.utils.GlobalConfig;
import com.gjf.wc.utils.SucceedAndFailedHandler;
import com.gjf.wc.utils.WechatHelper;

public class WechatShareTest {

    private static final String TAG = "WechatShareActivity";

    /**
     * 微信对话  链接分享
     */
    public static void shareLinkToWechat() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setLinkUrl("https://github.com/flora-gjf/WeChatHelper");
        params.setShareType(ShareType.LINK);

        shareToWechat(params);
    }

    /**
     * 微信对话  网络图片分享
     */
    public static void shareNetworkImageToWechat() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setNetworkImage("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1521640773430&di=726ad8d9bd244783e9c2ef97e01cab8f&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F18d8bc3eb13533fa02488324a2d3fd1f41345b39.jpg");
        params.setShareType(ShareType.NETWORK_IMAGE);

        shareToWechat(params);
    }

    /**
     * 微信对话  本地图片分享
     */
    public static void shareLocalImageToWechat() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setLocalBitmap(BitmapFactory
                .decodeResource(GlobalConfig.getAppContext().getResources(), R.mipmap.ic_launcher));
        params.setShareType(ShareType.LOCAL_IMAGE);

        shareToWechat(params);
    }

    /**
     * 微信朋友圈  链接分享
     */
    public static void shareLinkToWechatMoments() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setLinkUrl("https://github.com/flora-gjf/WeChatHelper");
        params.setShareType(ShareType.LINK);

        shareToWechatMoments(params);
    }

    /**
     * 微信朋友圈  网络图片分享
     */
    public static void shareNetworkImageToWechatMoments() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setNetworkImage("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1521640773430&di=726ad8d9bd244783e9c2ef97e01cab8f&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F18d8bc3eb13533fa02488324a2d3fd1f41345b39.jpg");
        params.setShareType(ShareType.NETWORK_IMAGE);

        shareToWechatMoments(params);
    }

    /**
     * 微信朋友圈  本地图片分享
     */
    public static void shareLocalImageToWechatMoments() {
        ShareParams params = new ShareParams();
        params.setTitle("title");
        params.setLocalBitmap(BitmapFactory
                .decodeResource(GlobalConfig.getAppContext().getResources(), R.mipmap.ic_launcher));
        params.setShareType(ShareType.LOCAL_IMAGE);

        shareToWechatMoments(params);
    }

    public static void shareToWechatMoments(ShareParams shareParams) {
        WechatHelper.getInstance().shareWechatMoments(shareParams, new SucceedAndFailedHandler() {
            @Override
            public void onSuccess(Object obj) {
                Log.i(TAG, "微信朋友圈分享成功");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.i(TAG, "微信朋友圈分享失败");
            }
        });
    }

    public static void shareToWechat(ShareParams shareParams) {
        WechatHelper.getInstance().shareWechat(shareParams, new SucceedAndFailedHandler() {
            @Override
            public void onSuccess(Object obj) {
                Log.i(TAG, "微信对话分享成功");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.i(TAG, "微信对话分享失败");
            }
        });
    }
}
