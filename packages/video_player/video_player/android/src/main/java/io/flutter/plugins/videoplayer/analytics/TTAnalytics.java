package io.flutter.plugins.videoplayer.analytics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bitmovin.analytics.BitmovinAnalyticsConfig;
import com.bitmovin.analytics.enums.PlayerType;
import com.bitmovin.analytics.exoplayer.ExoPlayerCollector;
import com.google.android.exoplayer2.ExoPlayer;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class TTAnalytics implements MethodChannel.MethodCallHandler {
    private static final TTAnalytics sInstance = new TTAnalytics();
    //    private static final String KEY = "35B0FD19-C1A4-468A-B99F-14CAA5A3A2FD";
//    private static final String KEY = "4E4EC445-31FA-4A8F-9F96-8EE86A9CB190";
    private static final String METHOD_CHANNEL = "videoplayer/analytics";

    private BitmovinAnalyticsConfig mConfig;
    private ExoPlayerCollector mAnalytics;
    private MethodChannel mChannel;
    private Context mContext;
    private boolean isInit = false;
    private String lastId = "";

    public static TTAnalytics shared() {
        return sInstance;
    }

    private TTAnalytics() {
    }

    public ExoPlayerCollector getAnalytics() {
        return mAnalytics;
    }

    public void init(Context context, BinaryMessenger binaryMessenger) {
        mContext = context;
        mChannel = new MethodChannel(binaryMessenger, METHOD_CHANNEL);
        mChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "initAnalytics":
                initAnalytics(call);
                break;
            case "setAnalyticsConfig":
                setAnalyticsConfig(call);
                break;
            default:
                break;
        }
        result.success(null);
    }

    private void initAnalytics(@NonNull MethodCall caller) {
        if (caller.hasArgument("analytics_key")) {
            String key = caller.argument("analytics_key");
            if (key != null) {
                mConfig = new BitmovinAnalyticsConfig(key);
                mConfig.setPlayerType(PlayerType.EXOPLAYER);
                mAnalytics = new ExoPlayerCollector(mConfig, mContext);
                isInit = true;
            }
        }
    }

    private void setAnalyticsConfig(@NonNull MethodCall caller) {
        if (!isInit) {
            return;
        }
        if (caller.hasArgument("title")) {
            mConfig.setTitle(caller.argument("title"));
        } else {
            mConfig.setTitle(null);
        }
        if (caller.hasArgument("video_id")) {
            mConfig.setVideoId(caller.argument("video_id"));
        } else {
            mConfig.setVideoId(null);
        }
        if (caller.hasArgument("is_live")) {
            mConfig.setIsLive(caller.argument("is_live"));
        } else {
            mConfig.setIsLive(null);
        }
        if (caller.hasArgument("video_url")) {
            mConfig.setM3u8Url(caller.argument("video_url"));
        } else {
            mConfig.setM3u8Url(null);
        }
        if (caller.hasArgument("user_id")) {
            mConfig.setCustomUserId(caller.argument("user_id"));
        } else {
            mConfig.setCustomUserId(null);
        }
    }

    public void detachPlayer(String id) {
        if (isInit && lastId.equals(id)) {
            lastId = "";
            mAnalytics.detachPlayer();
        }
    }

    public void attachPlayer(ExoPlayer player, String id) {
        if (isInit) {
            if (!lastId.isEmpty()) {
                mAnalytics.detachPlayer();
            }
            lastId = id;
            mAnalytics.attachPlayer(player);
        }
    }
}
