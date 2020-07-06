package com.app.uberclone;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("1Akff7kl8yH9I6jprY2g1x4viybZ9NH9APDXmw0o")
                // if defined
                .clientKey("hT3tdcnoA3xADeOzaWqPaVoj9dyLy9K3rHeLIk0h")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}
