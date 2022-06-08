package io.flutter.plugins.videoplayer.analytics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        mConfig.setCustomData1(caller.argument("customData1"));
        mConfig.setCustomData2(caller.argument("customData2"));
        mConfig.setCustomData3(caller.argument("customData3"));
        mConfig.setCustomData4(caller.argument("customData4"));
        mConfig.setCustomData5(caller.argument("customData5"));
        mConfig.setCustomData6(caller.argument("customData6"));
        mConfig.setCustomData7(caller.argument("customData7"));
        mConfig.setCustomData8(caller.argument("customData8"));
        mConfig.setCustomData9(caller.argument("customData9"));
        mConfig.setCustomData10(caller.argument("customData10"));
        mConfig.setCustomData11(caller.argument("customData11"));
        mConfig.setCustomData12(caller.argument("customData12"));
        mConfig.setCustomData13(caller.argument("customData13"));
        mConfig.setCustomData14(caller.argument("customData14"));
        mConfig.setCustomData15(caller.argument("customData15"));
        mConfig.setCustomData16(caller.argument("customData16"));
        mConfig.setCustomData17(caller.argument("customData17"));
        mConfig.setCustomData18(caller.argument("customData18"));
        mConfig.setCustomData19(caller.argument("customData19"));
        mConfig.setCustomData20(caller.argument("customData20"));
        mConfig.setCustomData21(caller.argument("customData21"));
        mConfig.setCustomData22(caller.argument("customData22"));
        mConfig.setCustomData23(caller.argument("customData23"));
        mConfig.setCustomData24(caller.argument("customData24"));
        mConfig.setCustomData25(caller.argument("customData25"));
        mConfig.setCustomData26(caller.argument("customData26"));
        mConfig.setCustomData27(caller.argument("customData27"));
        mConfig.setCustomData28(caller.argument("customData28"));
        mConfig.setCustomData29(caller.argument("customData29"));
        mConfig.setCustomData30(caller.argument("customData30"));
        mConfig.setAds(value(caller.argument("ads"), false));
        mConfig.setTitle(caller.argument("title"));
        mConfig.setVideoId(caller.argument("videoId"));
        mConfig.setIsLive(value(caller.argument("isLive"), false));
        mConfig.setCustomUserId(caller.argument("customerUserId"));
        mConfig.setExperimentName(caller.argument("experimentName"));
        mConfig.setPath(caller.argument("path"));
        mConfig.setRandomizeUserId(value(caller.argument("randomizeUserId"), false));
        mConfig.setHeartbeatInterval(value(caller.argument("heartbeatInterval"), 59_000));
    }

    private <T> T value(@Nullable T inValue, T def) {
        if (inValue != null) {
            return inValue;
        }
        return def;
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
