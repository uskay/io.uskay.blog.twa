package io.uskay.blog.twa;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdInfoSingleton.generate(this);
    }
}
