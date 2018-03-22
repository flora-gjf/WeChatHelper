# 微信授权分享工具类 WeChatHelper 

### 功能介绍

* 微信授权
* 微信分享

### 使用方式

* 请在在`UserConstants`类中设置`AppID`及`AppSecret`
* 请在需要接收微信回调页面增加`WechatObserver`处理回调后的结果(参见MainActivity.WechatAuthObserver)
* 请在发起微信授权时调用`WechatHelper.getInstance().authorizeByWechat()`

> 请注意观察者收到回调后的线程问题

### 方法说明

##### 微信授权

`WechatHelper.getInstance().authorizeByWechat()` 

##### 微信分享

1. 对话分享接口: 

  `WechatHelper.getInstance().shareWechat(ShareParams params)`
  
   使用参考：
    
   | 分享类型 | 具体方法 | 
   | ------------- |-------------|
   链接分享 |  WechatShareTest.shareLinkToWechat
   网络图片分享|  WechatShareTest.shareNetworkImageToWechat
   本地图片分享|  WechatShareTest.shareLocalImageToWechat
   
2. 朋友圈分享接口: 
  
  `WechatHelper.getInstance().shareWechatMoments(ShareParams params)`
  
   使用参考：
   
   | 分享类型 | 具体方法 | 
   | ------------- |-------------|
   链接分享 |  WechatShareTest.shareLinkToWechatMoments
   网络图片分享|  WechatShareTest.shareNetworkImageToWechatMoments
   本地图片分享|  WechatShareTest.shareLocalImageToWechatMoments

3. 分享参数ShareParams

   | ShareParams属性 | 功能 | 取值 | 
   | ------------- |-------------|-------------|
   mShareType | 分享类型 | `ShareType.LOCAL_IMAGE`(本地图片分享)，`ShareType.NETWORK_IMAGE`(网络图片分享)，`ShareType.LINK`(链接分享)
   mTitle | 分享标题 | String类型
   mText | 分享内容| String类型
   mLinkUrl | 分享链接| String类型
   mNetworkImageUrl | 分享网络图片链接| String类型
   mLocalBitmap | 分享本地图片| Bitmap类型

### 备注

Demo中未设置正确AppID／AppSecret，直接运行是没有效果的。

欢迎关注小可爱的简书[Jimmy_gjf ](https://www.jianshu.com/u/02bba7269ac1)❤️❤️


