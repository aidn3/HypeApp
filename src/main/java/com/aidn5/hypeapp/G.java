package com.aidn5.hypeapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aidn5.hypeapp.services.IgnProvider;
import com.evernote.android.job.JobRequest;

import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraNotification;
import org.acra.annotation.AcraScheduler;

@AcraCore(buildConfigClass = BuildConfig.class, reportSenderFactoryClasses = ReportService.class)
@AcraScheduler(requiresNetworkType = JobRequest.NetworkType.UNMETERED, requiresBatteryNotLow = true)
@AcraLimiter()
@AcraNotification(resTitle = R.string.unhandled_exception, resText = R.string.unhandled_exception_message, resChannelName = R.string.app_notification_channel)
public class G extends Application {
    private static IgnProvider ignProvider;
    private static SharedPreferences settings;

    public static IgnProvider getIgnProvider(Context context) {
        return ignProvider != null ? ignProvider : (ignProvider = new IgnProvider(context));
    }

    public static SharedPreferences getSettings(Context context) {
        return settings != null ? settings : (settings = PreferenceManager.getDefaultSharedPreferences(context));
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        });
    }*/

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        //setACRA();
    }

    /*private void setACRA() {
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);

        //noinspection unchecked
        builder
                .setEnabled(true)
                .setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.JSON)
                .setReportSenderFactoryClasses(ReportService.class);

        builder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class)
                .setEnabled(true);

        builder.getPluginConfigurationBuilder(SchedulerConfigurationBuilder.class)
                .setEnabled(true)
                .setRequiresNetworkType(JobRequest.NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true);

        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder.class)
                .setEnabled(true)
                .setResTitle(R.string.unhandled_exception)
                .setResText(R.string.unhandled_exception_message);


        ACRA.DEV_LOGGING = true;
        ACRA.init(this, builder);
        //ACRA.init(this);
    }*/
}