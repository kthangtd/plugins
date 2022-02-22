package io.flutter.plugins.videoplayer.widget;

import android.content.Context;
import android.view.View;

import java.util.HashMap;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class TTNativeViewFactory extends PlatformViewFactory {

    private final BinaryMessenger binaryMessenger;


    public TTNativeViewFactory(BinaryMessenger binaryMessenger) {
        super(StandardMessageCodec.INSTANCE);
        this.binaryMessenger = binaryMessenger;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        try {
            return new TTNativeVideoPlayer(context, viewId, (HashMap) args, binaryMessenger);
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