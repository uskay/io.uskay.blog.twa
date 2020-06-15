package io.uskay.blog.twa;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

public class AdInfoSingleton {

    private static AdvertisingIdClient.Info adInfo;

    public static void generate(Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo =
                            AdvertisingIdClient.getAdvertisingIdInfo(context);
                    AdInfoSingleton.adInfo = adInfo;
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static AdvertisingIdClient.Info get() {
        return adInfo;
    }

}
