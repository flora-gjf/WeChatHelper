package com.gjf.wc.share;

import android.graphics.Bitmap;

/**
 * 分享参数类:
 * <p>
 * mShareType 分享类型: 本地图片分享，网络图片分享，链接分享等
 * mTitle 分享标题
 * mText 分享内容
 * mLinkUrl 分享链接
 * mNetworkImageUrl 分享网络图片链接
 * mLocalBitmap 分享本地图片
 *
 * @author guojunfu
 */
public class ShareParams {
    private int mShareType;

    private String mTitle = "";
    private String mText = "";
    private String mLinkUrl = "";
    private String mNetworkImageUrl = "";

    private Bitmap mLocalBitmap;

    /**
     * 分享类型
     */
    public void setShareType(int shareType) {
        this.mShareType = shareType;
    }

    /**
     * 分享标题
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * 分享文本  分享到新浪微博时一定要设置text
     */
    public void setText(String text) {
        this.mText = text;
    }

    /**
     * 分享链接
     */
    public void setLinkUrl(String urlString) {
        this.mLinkUrl = urlString;
    }

    /**
     * 分享网络图片
     */
    public void setNetworkImage(String url) {
        mNetworkImageUrl = url;
    }

    /**
     * 分享本地图片
     */
    public void setLocalBitmap(Bitmap shareBitmap) {
        mLocalBitmap = shareBitmap;
    }

    public int getShareType() {
        return mShareType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

    public String getLinkUrl() {
        return mLinkUrl;
    }

    public Bitmap getLocalBitmap() {
        return mLocalBitmap;
    }

    public String getNetworkImageUrl() {
        return mNetworkImageUrl;
    }
}
