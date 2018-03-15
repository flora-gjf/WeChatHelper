package com.gjf.wc.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.gjf.wc.utils.GlobalConfig;
import com.gjf.wc.utils.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by guojunfu on 18/3/15.
 */

public class OkHttpHelper {
    private static OkHttpHelper sInstance;

    private static final int UnknownException = 2;
    private static final int NetworkException = -1;
    private static final int DealResException = -2;

    private OkHttpClient mClient = null;

    public OkHttpHelper() {}

    public static synchronized OkHttpHelper getInstance() {
        if(sInstance == null) {
            sInstance = new OkHttpHelper();
        }

        return sInstance;
    }

    public static NetworkHandler<String> getStrResOnUi(final NetworkHandler<String> leHandler) {
        return new NetworkHandler<String>() {
            @Override
            public void onSuccess(final String response) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onSuccess(response);
                    }
                });
            }

            @Override
            public void onFailure(final int errorCode) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onFailure(errorCode);
                    }
                });
            }
        };
    }

    public static NetworkHandler<byte[]> getByteResOnUi(final NetworkHandler<byte[]> leHandler) {
        return new NetworkHandler<byte[]>() {
            @Override
            public void onSuccess(final byte[] response) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onSuccess(response);
                    }
                });
            }

            @Override
            public void onFailure(final int errorCode) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onFailure(errorCode);
                    }
                });
            }
        };
    }

    public static NetworkHandler<Object> getObjectResOnUi(final NetworkHandler<Object> leHandler) {
        return new NetworkHandler<Object>() {
            @Override
            public void onSuccess(final Object response) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onSuccess(response);
                    }
                });
            }

            @Override
            public void onFailure(final int errorCode) {
                ThreadPool.runOnUi(new Runnable() {
                    @Override
                    public void run() {
                        leHandler.onFailure(errorCode);
                    }
                });
            }
        };
    }

    private OkHttpClient getClient() {
        if(this.mClient == null) {
            this.mClient = (new OkHttpClient.Builder()).build();
        }

        return this.mClient;
    }

    public void setOkHttpClient(OkHttpClient paramClient) {
        this.mClient = paramClient;
    }

    public void requestStringGet(String url, NetworkHandler vh) {
        this.requestStringGet(url, vh, 2500);
    }

    public void requestStringGet(String url, NetworkHttpParam para, NetworkHandler vh) {
        String mFormatUrl;
        if(para == null) {
            mFormatUrl = url;
        } else {
            mFormatUrl = this.formatUrl(para.getPara(), url);
        }

        this.requestStringGet(mFormatUrl, vh, 2500);
    }

    public void requestStringGet(String url, NetworkHandler vh, int timeOut) {
        this.sendRequest(url, vh, (Object)null, RequestType.Get, ResponseType.Str, (Object)null);
    }

    public void requestBytePost(String url, NetworkHandler vh, NetworkHttpParam para) {
        this.requestBytePost(url, vh, para, RequestType.Post);
    }

    public void requestByteGet(String url, NetworkHandler vh, NetworkHttpParam para) {
        String mFormatUrl;
        if(para == null) {
            mFormatUrl = url;
        } else {
            mFormatUrl = this.formatUrl(para.getPara(), url);
        }

        this.sendRequest(mFormatUrl, vh, (Object)null, RequestType.Get, ResponseType.Bytes, (Object)null);
    }

    public void requestBytePost(String url, NetworkHandler vh, NetworkHttpParam para, RequestType requestType) {
        this.sendRequest(url, vh, para, RequestType.Post, ResponseType.Bytes, (Object)null);
    }

    public void requestStringPost(String url, NetworkHandler vh, NetworkHttpParam para, boolean shouldCache) {
        this.requestStringPost(url, vh, para, 2500, (Object)null, shouldCache);
    }

    public void requestStringPost(String url, NetworkHandler vh, NetworkHttpParam para) {
        this.requestStringPost(url, vh, para, 2500, (Object)null, true);
    }

    public void requestStringPost(String url, NetworkHandler vh, NetworkHttpParam para, Object tag) {
        this.requestStringPost(url, vh, para, 2500, tag, true);
    }

    public void requestStringPost(String url, NetworkHandler vh, NetworkHttpParam para, int timeOut, Object tag) {
        this.requestStringPost(url, vh, para, timeOut, tag, true);
    }

    public void requestStringPost(String url, NetworkHandler vh, NetworkHttpParam para, int timeOut, Object tag, boolean shouldCache) {
        this.sendRequest(url, vh, para, RequestType.Post, ResponseType.Str, tag);
    }

    public void requestMultipartPost(String url, NetworkHandler vh, NetworkMultipartParams params) {
        this.requestMultipartStrPost(url, vh, params);
    }

    public void requestMultipartBytePost(String url, NetworkHandler vh, NetworkMultipartParams params) {
        this.sendRequest(url, vh, params, RequestType.Post, ResponseType.Bytes, (Object)null);
    }

    public void requestMultipartStrPost(String url, NetworkHandler vh, NetworkMultipartParams params) {
        this.sendRequest(url, vh, params, RequestType.Post, ResponseType.Str, (Object)null);
    }

    private <Params> void sendRequest(String url, final NetworkHandler vh, Params params,
                                      RequestType requestType, final ResponseType responseType, Object tag) {
        OkHttpClient client = this.getClient();
        Object requestBody = null;

        try {
            Map paramsStr;
            String value;
            String key;

            if(params instanceof NetworkMultipartParams) {
                NetworkMultipartParams multiParams = (NetworkMultipartParams)params;
                okhttp3.MultipartBody.Builder builder = (new okhttp3.MultipartBody.Builder()).setType(MultipartBody.FORM);
                paramsStr = multiParams.getParamsStr();
                Map paramsBytes = multiParams.getParamsBytes();
                Iterator entries;
                Map.Entry entry;
                if(paramsStr != null) {
                    entries = paramsStr.entrySet().iterator();

                    while(entries.hasNext()) {
                        entry = (Map.Entry)entries.next();
                        key = (String)entry.getKey();
                        value = (String)entry.getValue();
                        builder.addFormDataPart(key, value);
                    }
                }

                if(paramsBytes != null) {
                    entries = paramsBytes.entrySet().iterator();

                    while(entries.hasNext()) {
                        entry = (Map.Entry)entries.next();
                        key = (String)entry.getKey();
                        byte[] valueByte = (byte[])((byte[])entry.getValue());
                        builder.addFormDataPart(key, key, RequestBody.create((MediaType)null, valueByte));
                    }
                }

                requestBody = builder.build();
            }

            if(params instanceof NetworkHttpParam) {
                NetworkHttpParam httpParams = (NetworkHttpParam)params;
                okhttp3.FormBody.Builder builder = new okhttp3.FormBody.Builder();
                paramsStr = httpParams.getPara();
                if(paramsStr != null) {
                    Iterator entries = paramsStr.entrySet().iterator();

                    while(entries.hasNext()) {
                        Map.Entry entry = (Map.Entry)entries.next();
                        key = (String)entry.getKey();
                        value = (String)entry.getValue();
                        builder.add(key, value);
                    }
                }

                requestBody = builder.build();
            }

            okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
            if(tag != null) {
                builder.tag(tag);
            }

            Map<String, String> para = new ArrayMap();
            Context context = GlobalConfig.getAppContext();
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            para.put("v", versionName + " android");
            url = this.formatUrl(para, url);
            Request request;

            if(requestType == OkHttpHelper.RequestType.Post) {
                if(requestBody == null) {
                    requestBody = (new okhttp3.FormBody.Builder()).build();
                }

                request = builder.url(url).post((RequestBody)requestBody).build();
            } else {
                request = builder.url(url).get().build();
            }

            Callback callback = new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    vh.onFailure(-1);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    OkHttpHelper.this.dealResponse(response, responseType, vh);
                }
            };
            client.newCall(request).enqueue(callback);
        } catch (Exception var17) {
            var17.printStackTrace();
            vh.onFailure(2);
        }

    }

    private void dealResponse(Response res, OkHttpHelper.ResponseType type, NetworkHandler vh) {
        try {
            if(null == res) {
                return;
            }

            if(!res.isSuccessful() || null == res.body()) {
                vh.onFailure(res.code());
                return;
            }

            if(type.equals(OkHttpHelper.ResponseType.Str)) {
                vh.onSuccess(res.body().string());
                return;
            }

            if(!type.equals(OkHttpHelper.ResponseType.Bytes)) {
                throw new IllegalArgumentException("Unhandled class: " + type + " for Res.body()");
            }

            vh.onSuccess(res.body().bytes());
        } catch (Exception var8) {
            var8.printStackTrace();
            vh.onFailure(-2);
            return;
        } finally {
            if(null != res) {
                res.close();
            }
        }

    }

    private String formatUrl(Map<String, String> map, String url) {
        if(map.isEmpty()) {
            return url;
        } else {
            String formatUrl;
            if(url.contains("?")) {
                formatUrl = url + "&";
            } else {
                formatUrl = url + "?";
            }

            String key;
            for(Iterator var4 = map.keySet().iterator(); var4.hasNext(); formatUrl = formatUrl + key + "=" + (String)map.get(key) + "&") {
                key = (String)var4.next();
            }

            formatUrl = formatUrl.substring(0, formatUrl.length() - 1);
            return formatUrl;
        }
    }

    public static boolean isGifUrl(String url) {
        return TextUtils.isEmpty(url)?false:url.endsWith(".gif");
    }

    public static boolean isValidUrl(String url) {
        try {
            return Patterns.WEB_URL.matcher(url).matches();
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isFileExist(String path) {
        try {
            File file = new File(path);
            if(file.exists()) {
                return true;
            }
        } catch (Exception var2) {
            ;
        }

        return false;
    }

    public void requestImage(ImageView imageView, String url, int errorImage, int placeHolderImage) {
//        if(placeHolderImage == 0) {
//            placeHolderImage = color.white;
//        }
//
//        if(errorImage == 0) {
//            errorImage = color.white;
//        }

        if(!isValidUrl(url) && !isFileExist(url)) {
            Glide.with(imageView.getContext()).load(Integer.valueOf(placeHolderImage)).centerCrop().fitCenter().into(imageView);
        } else if(isGifUrl(url)) {
            Glide.with(imageView.getContext()).load(url).asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).skipMemoryCache(true)
                    .placeholder(placeHolderImage).error(errorImage).centerCrop().into(imageView);
        } else {
            BitmapRequestBuilder builder = Glide.with(imageView.getContext())
                    .load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(placeHolderImage).error(errorImage).centerCrop();
            if(!isValidUrl(url)) {
                builder.signature(new StringSignature((new File(url)).lastModified() + ""));
            }

            builder.into(imageView);
        }
    }

    public void requestImage(Context context, String url, int errorImage,
                             int placeHolderImage, ViewTarget target) {
        if(target != null) {
            Glide.with(context).load(url).placeholder(placeHolderImage).error(errorImage).centerCrop().fitCenter().into(target);
        }
    }

    public void requestRoundImage(ImageView imageView, String url, int errorImage, int placeHolderImage) {
        this.requestCornerImage((float)(imageView.getWidth() / 2), imageView, url, errorImage, placeHolderImage);
    }

    public void requestCornerImage(final float cornerRadius, final ImageView imageView,
                                   String url, int errorImage, int placeHolderImage) {
        Glide.with(imageView.getContext()).load(url).asBitmap().placeholder(placeHolderImage)
                .skipMemoryCache(true).error(errorImage).centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(imageView.getContext().getResources(), resource);
                circularBitmapDrawable.setCornerRadius(cornerRadius);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public void requestFitXYImage(ImageView imageView, String url, int errorImage, int placeHolderImage) {
//        if(placeHolderImage == 0) {
//            placeHolderImage = color.white;
//        }
//
//        if(errorImage == 0) {
//            errorImage = color.white;
//        }

        if(!isValidUrl(url) && !isFileExist(url)) {
            Glide.with(imageView.getContext()).load(Integer.valueOf(placeHolderImage)).into(imageView);
        } else if(isGifUrl(url)) {
            Glide.with(imageView.getContext()).load(url).asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).skipMemoryCache(true)
                    .placeholder(placeHolderImage).error(errorImage).into(imageView);
        } else {
            BitmapRequestBuilder builder = Glide.with(imageView.getContext())
                    .load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(placeHolderImage).error(errorImage);
            if(!isValidUrl(url)) {
                builder.signature(new StringSignature((new File(url)).lastModified() + ""));
            }

            builder.into(imageView);
        }
    }

    public void downloadBitmap(final String url, final NetworkHandler handler) {
        ThreadPool.runOnPool(new Runnable() {
            @Override
            public void run() {
                FutureTarget futureTarget = Glide.with(GlobalConfig.getAppContext())
                        .load(url).downloadOnly(-2147483648, -2147483648);

                try {
                    if(futureTarget.get() != null) {
                        handler.onSuccess(url);
                        return;
                    }
                } catch (InterruptedException var3) {
                    var3.printStackTrace();
                } catch (ExecutionException var4) {
                    var4.printStackTrace();
                }

                handler.onFailure(2);
            }
        });
    }

    public void requestBitmapFromUrl(String url, int width, int height, final NetworkHandler<Bitmap> vh) {
        Target target = new SimpleTarget<Bitmap>(width, height) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                vh.onSuccess(resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                vh.onFailure(-1);
            }
        };
        this.requestBitmapFromUrl(url, (Target)target);
    }

    public void requestBitmapFromUrl(String url, final NetworkHandler<Bitmap> vh) {
        Target target = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                vh.onSuccess(resource);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                vh.onFailure(-1);
            }
        };
        this.requestBitmapFromUrl(url, (Target)target);
    }

    public void requestBitmapFromUrl(String url, Target target) {
        BitmapTypeRequest request = Glide.with(GlobalConfig.getAppContext()).load(url).asBitmap();
        if(!isValidUrl(url)) {
            request.signature(new StringSignature((new File(url)).lastModified() + ""));
        }

        request.into(target);
    }

    public Bitmap requestBitmapFromUrl(String url, int width, int height) {
        try {
            BitmapTypeRequest request = Glide.with(GlobalConfig.getAppContext()).load(url).asBitmap();
            if(!isValidUrl(url)) {
                request.signature(new StringSignature((new File(url)).lastModified() + ""));
            }

            return (Bitmap)request.into(width, height).get();
        } catch (InterruptedException var5) {
            var5.printStackTrace();
        } catch (ExecutionException var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public void cancel(Object tag) {
        if(this.mClient != null) {
            Iterator var2 = this.mClient.dispatcher().queuedCalls().iterator();

            Call call;
            while(var2.hasNext()) {
                call = (Call)var2.next();
                if(tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }

            var2 = this.mClient.dispatcher().runningCalls().iterator();

            while(var2.hasNext()) {
                call = (Call)var2.next();
                if(tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }

        }
    }

    enum ResponseType {
        Bytes,
        Str;

        ResponseType() {
        }
    }

    enum RequestType {
        Post,
        Get;

        RequestType() {
        }
    }
}








