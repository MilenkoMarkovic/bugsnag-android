package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.Configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CXXAutoContextScenario extends Scenario {

    static {
        System.loadLibrary("bugsnag-ndk");
        System.loadLibrary("monochrome");
        System.loadLibrary("entrypoint");
    }

    public native void activate();

    public CXXAutoContextScenario(@NonNull Configuration config,
                                  @NonNull Context context,
                                  @Nullable String eventMetadata) {
        super(config, context, eventMetadata);
        config.setAutoTrackSessions(false);
    }

    @Override
    public void startScenario() {
        super.startScenario();
        Context context = getContext();
        registerActivityLifecycleCallbacks();
        context.startActivity(new Intent("com.bugsnag.android.mazerunner.UPDATE_CONTEXT"));
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activate();
    }
}
