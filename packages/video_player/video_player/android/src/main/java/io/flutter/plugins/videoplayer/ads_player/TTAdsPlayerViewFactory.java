package io.flutter.plugins.videoplayer.ads_player;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class TTAdsPlayerViewFactory extends PlatformViewFactory {

    private final BinaryMessenger binaryMessenger;


    public TTAdsPlayerViewFactory(BinaryMessenger binaryMessenger) {
        super(StandardMessageCodec.INSTANCE);
        this.binaryMessenger = binaryMessenger;

    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        try {
            return new TTAdsVideoPlayer(context, viewId, (Map<?, ?>) args, binaryMessenger);
        } catch (Exception e) {
            return new PlatformView() {
                @Override
                public View getView() {
                    return new View(context);
                }

                @Override
                public void dispose() {

                }
            };
        }
    }
}