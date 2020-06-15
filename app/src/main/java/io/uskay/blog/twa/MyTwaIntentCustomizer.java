package io.uskay.blog.twa;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.browser.trusted.TrustedWebActivityIntent;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.androidbrowserhelper.trusted.ITwaIntentCustomizer;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Used by the TWAIntentBuilderFactory referencing the metadata below in
 * AndroidManifest.xml
 * <meta-data
 *   android:name="com.google.androidbrowserhelper.trusted.intentcustomizer"
 *   android:value="io.uskay.blog.twa.MyTwaIntentCustomizer" />
 */
public class MyTwaIntentCustomizer implements ITwaIntentCustomizer {

    private final boolean SHOULD_USE_MOCK_DATA = true;

    @Override
    public TrustedWebActivityIntent getIntent
            (Context context, TrustedWebActivityIntent trustedWebActivityIntent) {
        Bundle customHeaders = new AppConversionTrackingMetaData(context).getMetaDataBundle();
        trustedWebActivityIntent.getIntent().putExtra(Browser.EXTRA_HEADERS, customHeaders);
        return trustedWebActivityIntent;
    }

    /**
     * https://developers.google.com/app-conversion-tracking/api/request-response-specs
     */
    class AppConversionTrackingMetaData {

        private final String SPACE = " ";
        private Context context;

        AppConversionTrackingMetaData(Context context) {
            this.context = context;
        }

        public Bundle getMetaDataBundle() {

            Bundle metaData = new Bundle();

            if(SHOULD_USE_MOCK_DATA){
                metaData.putString(addHeaderPrefix("mock-User-Agent"),
                        "uskay/0.0.1 (Android 10; en_US; " +
                                "Android SDK built for x86; Build/QSR1.190920.001)");
                metaData.putString(addHeaderPrefix("mock-rdid"),
                    "38400000-8cf0-11bd-b23e-10b96e40000d");
                metaData.putString(addHeaderPrefix("mock-id_type"), "advertisingid");
                metaData.putString(addHeaderPrefix("mock-lat"), "0");
                metaData.putString(addHeaderPrefix("mock-app_version"), "1");
                metaData.putString(addHeaderPrefix("mock-os_version"), "10");
                metaData.putString(addHeaderPrefix("mock-sdk_version"), "1");
                metaData.putString(addHeaderPrefix("timestamp"),
                        Long.toString(System.currentTimeMillis()));
                metaData.putString(addHeaderPrefix("gclid"), getGclid());
                return metaData;
            }

            // Format: MyAnalyticsCompany/1.0.0 (iOS 10.0.2; en_US; iPhone9,1; Build/13D15; Proxy)
            StringBuilder builder = new StringBuilder();
            builder.append("uskay/0.0.1");
            builder.append(SPACE);
            builder.append("(");
            builder.append("Android" + SPACE + Build.VERSION.RELEASE + ";");
            builder.append(SPACE);
            builder.append(Locale.getDefault() + ";");
            builder.append(SPACE);
            builder.append(Build.MODEL + ";");
            builder.append(SPACE);
            builder.append("Build/" + Build.ID);
            builder.append(")");
            metaData.putString(addHeaderPrefix("User-Agent"), builder.toString());

            // dev_token
            // link_id
            // app_event_type
            // app_event_name
            // app_event_data
            // rdid
            AdvertisingIdClient.Info adInfo = getAdInfo();
            metaData.putString(addHeaderPrefix("rdid"), getAdId(adInfo));
            // id_type
            metaData.putString(addHeaderPrefix("id_type"), "advertisingid");
            // lat
            metaData.putString(addHeaderPrefix("lat"),
                    isLimitAdTrackingEnabled(adInfo) ? "1" : "0");
            // app_version
            metaData.putString(addHeaderPrefix("app_version"), getAppVersion());
            // os_version
            metaData.putString(addHeaderPrefix("os_version"),
                    android.os.Build.VERSION.RELEASE);
            // sdk_version
            metaData.putString(addHeaderPrefix("sdk_version"), getAppVersion());
            // timestamp
            metaData.putString(addHeaderPrefix("timestamp"),
                    Long.toString(System.currentTimeMillis()));
            // value
            // currency_code
            // gclid
            metaData.putString(addHeaderPrefix("gclid"), getGclid());

            return metaData;
        }

        private String addHeaderPrefix(String headerName) {
            return "X-App-" + headerName;
        }


        private AdvertisingIdClient.Info getAdInfo() {
            return AdInfoSingleton.get();
        }

        private String getAdId(AdvertisingIdClient.Info adInfo) {
            if(adInfo == null) {
                return "";
            }
            return getAdInfo().getId();
        }

        private boolean isLimitAdTrackingEnabled(AdvertisingIdClient.Info adInfo) {
            if(adInfo == null) {
                return true;
            }
            return getAdInfo().isLimitAdTrackingEnabled();
        }

        private String getAppVersion() {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            return "";
        }

        private String getGclid() {
            Uri uri = ((Activity)context).getIntent().getData();
            if (uri == null) {
                return "";
            }
            return uri.getQueryParameter("gclid");
        }

    }
}
