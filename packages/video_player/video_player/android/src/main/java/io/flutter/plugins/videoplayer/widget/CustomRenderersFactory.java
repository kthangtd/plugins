//package io.flutter.plugins.videoplayer.widget;
//
//import android.content.Context;
//import android.os.Handler;
//
//import com.google.android.exoplayer2.DefaultRenderersFactory;
//import com.google.android.exoplayer2.ExoPlayer;
//import com.google.android.exoplayer2.Renderer;
//import com.google.android.exoplayer2.audio.AudioRendererEventListener;
//import com.google.android.exoplayer2.metadata.Metadata;
//import com.google.android.exoplayer2.metadata.MetadataOutput;
//import com.google.android.exoplayer2.metadata.MetadataRenderer;
//import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
//import com.google.android.exoplayer2.source.hls.HlsManifest;
//import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
//import com.google.android.exoplayer2.text.Cue;
//import com.google.android.exoplayer2.text.TextOutput;
//import com.google.android.exoplayer2.text.TextRenderer;
//import com.google.android.exoplayer2.video.VideoRendererEventListener;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//import io.flutter.plugin.common.MethodChannel;
//
//public class CustomRenderersFactory extends DefaultRenderersFactory {
//
//    private MethodChannel channel;
//    public ExoPlayer player;
//
//    public CustomRenderersFactory(Context context) {
//        super(context);
//    }
//
//    public CustomRenderersFactory(Context context, int extensionRendererMode) {
//        super(context, extensionRendererMode);
//    }
//
//    public CustomRenderersFactory(Context context, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
//        super(context, extensionRendererMode, allowedVideoJoiningTimeMs);
//    }
//
//    public CustomRenderersFactory(Context context, MethodChannel channel) {
//        this(context);
//        this.channel = channel;
//    }
//
//    @Override
//    public Renderer[] createRenderers(Handler eventHandler,
//                                      VideoRendererEventListener videoRendererEventListener,
//                                      AudioRendererEventListener audioRendererEventListener,
//                                      TextOutput textRendererOutput,
//                                      MetadataOutput metadataRendererOutput) {
//        ArrayList<Renderer> renderersList = new ArrayList<>();
//        Renderer[] renderers = super.createRenderers(eventHandler, videoRendererEventListener,
//                audioRendererEventListener, textRendererOutput, metadataRendererOutput);
//        renderersList.addAll(Arrays.asList(renderers));
//        renderersList.add(
//                new TextRenderer(cues -> {
//                    if (cues.size() > 0) {
//                        Cue cue = cues.get(0);
//                        if (cue.text == null || cue.text == "") {
//                            return;
//                        }
//                        Map<String, Object> event = new HashMap<>();
//                        event.put("event", "subtitle");
//                        event.put("values", cue.text.toString());
//                        eventSinkSuccess(event);
//                    }
//                }, eventHandler.getLooper()));
//        renderersList.add(new MetadataRenderer(metadata -> {
//            for (int i = 0; i < metadata.length(); i++) {
//                Metadata.Entry entry = metadata.get(i);
//                if (entry instanceof TextInformationFrame) {
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("key", ((TextInformationFrame) entry).value.split("_")[0]);
//                    map.put("meta", ((TextInformationFrame) entry).value);
//                    map.put("is_ad", isMediaMetadataAd(((TextInformationFrame) entry).value));
//                    Map<String, Object> event = new HashMap<>();
//                    event.put("event", "mediaMetadataChanged");
//                    event.put("values", map);
//                    eventSinkSuccess(event);
//                }
//            }
//        }, eventHandler.getLooper()));
//        return renderersList.toArray(new Renderer[0]);
//    }
//
//    private void eventSinkSuccess(Map<String, Object> values) {
//        if (channel != null) {
//            channel.invokeMethod("nativeCall", values);
//        }
//    }
//
//    private boolean isMediaMetadataAd(String mediaDataValue) {
//        String id = mediaDataValue.split("_")[0];
//        if (player != null && player.getCurrentManifest() instanceof HlsManifest) {
//            HlsManifest manifest = (HlsManifest) player.getCurrentManifest();
//            for (HlsMediaPlaylist.Segment seg : manifest.mediaPlaylist.segments) {
//                if (seg.url.contains(id) && seg.url.contains("is_ad=1")) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//}
