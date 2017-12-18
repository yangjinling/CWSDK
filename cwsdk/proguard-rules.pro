# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\andriod\SDK\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5

# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses

# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose

# 指定不去忽略非公共库的类成员
-dontskipnonpubliclibraryclassmembers

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable,InnerClass

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

-dontshrink
-ignorewarnings
# 保留我们使用的四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService


# 保留support下的所有类及其内部类
-keep class android.support.** {*;}

# 保留在Activity中的方法参数是view的方法，
# 这样以来我们在layout中写的onClick就不会被影响
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


-libraryjars 'C:\Program Files (x86)\Java\jdk1.8.0_71\jre\lib\rt.jar'
-keep class * extends android.content.BroadcastReceiver {
    public void onReceive(android.content.Context,android.content.Intent);
}
# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
#public void * (android.content.Context,android.content.Intent);
#public void onReceive(android.content.Context,android.content.Intent);

-keep class * extends android.content.BroadcastReceiver {
*;
}
#public void onReceive(android.content.Context,android.content.Intent);
-keepclassmembers class * extends android.content.BroadcastReceiver {
*;
}
-keep class cwbjsdk.cwsdk.sdk.*{*;}





# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class cwbjsdk.cwsdk.util.BJCWUtil{
     public <fields>;
     public <methods>;
}
-keep class cwbjsdk.cwsdk.util.SMS4Class{
     public <fields>;
     public <methods>;
 }
-keep class cwbjsdk.cwsdk.bean.*{
#     public <fields>;
#     public <methods>;
*;
 }

#ble通讯身份解析不混淆
 -keep class cwbjsdk.cwsdk.util.ParserIdentity{
      public <fields>;
      public <methods>;
  }
-keep public interface cwbjsdk.cwsdk.util.DataBeanCallback{
public <methods>;
}
-keep public class * implements cwbjsdk.cwsdk.util.DataBeanCallback{
  public <methods>;
}

