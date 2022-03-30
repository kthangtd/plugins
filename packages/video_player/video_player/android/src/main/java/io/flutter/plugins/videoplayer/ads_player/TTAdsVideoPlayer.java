package io.flutter.plugins.videoplayer.ads_player;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugins.videoplayer.R;
import io.flutter.plugins.videoplayer.widget.BitmapOverlayVideoProcessor;
import io.flutter.plugins.videoplayer.widget.VideoProcessingGLSurfaceView;

public class TTAdsVideoPlayer implements PlatformView, Player.Listener {

    @Nullable
    private final PlayerView playerView;

    @Nullable
    private final VideoProcessingGLSurfaceView videoProcessingGLSurfaceView;

    private SimpleExoPlayer exoPlayer;
    private MethodChannel channel;
    private boolean isInitialized = false;
    private DefaultTrackSelector trackSelector;
    private final int viewId;
    private final ImaAdsLoader adsLoader;

    @Nullable
    private final String vastTagUrl;

    @Override
    public View getView() {
        return playerView;
    }

    @Override
    public void dispose() {
        if (playerView != null) {
            playerView.onPause();
        }
        releasePlayer();
    }

    private Uri getAdTagUri() {
        return Uri.parse(vastTagUrl);
//        return Uri.parse("<![CDATA[https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=]]>");
//        return Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=");
    }

    private Uri getAdTagPreroll() {
        return Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=");
    }

    private Uri getAdTagDemo1() {
        return Uri.parse("https://pbs.getpublica.com/v1/s2s-hb?valid=1&type=spotx&player_width=1080&player_height=1920&_v=2&site_id=11108&app_bundle=com.butacatv.butaca_app&preroll=1&did=0801e30a-71d1-442a-ab5d-f3a089031bb7&app_name=butaca_androidtv&app_store_url=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.butacatv.butaca_app&format=vast&slot_count=4&cb=1648532555428&ip=189.217.75.19&content_id=cin01266&content_title=Cuando+los+hijos+regresan&us_privacy=1&content_genre=drama&content_length=5400&content_prodqual=1&verandaid=cin01266&max_ad_duration=30&min_ad_duration=6&livestream=1&xff=189.217.75.19&ses_id=1648532555428&vid_id=734c5bac51e047bca9fbc66acfa0d8c9&vid_title=Cuando+los+hijos+regresan+-+Cinepolis%2FCuando_Los_Hijos_Regresan_1920x1080_23.98_20Mbps.mp4&vid_description=Cuando+los+hijos+regresan+-+Cinepolis%2FCuando_Los_Hijos_Regresan_1920x1080_23.98_20Mbps.mp4&vpaid=0&ssai%5Benabled%5D=1&ssai%5Bvendor%5D=uplynk&custom%5Bpre%5D=0&custom%5Bmid%5D=1&ua=Mozilla%2F5.0+%28SMART-TV%3B+Linux%3B+Tizen+4.0%29+AppleWebKit%2F538.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+TV+Safari%2F538");
    }

    private Uri getAdTagDemo2() {
        return Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator=");
    }

    private Uri getAdTagDemo3() {
        return Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpostlongpod&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator=");
    }

    public TTAdsVideoPlayer(Context context, int id, Map<?, ?> params,
                            BinaryMessenger binaryMessenger) throws Exception {

        this.viewId = id;
        this.vastTagUrl = String.valueOf(params.get("vast_tag_url"));
        adsLoader = new ImaAdsLoader.Builder(context).build();

        initializeChannel(binaryMessenger);
        playerView = new PlayerView(context);
//        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setUseController(false);
        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView =
                new VideoProcessingGLSurfaceView(
                        context,
                        false,
                        new BitmapOverlayVideoProcessor(context));
        FrameLayout contentFrame = playerView.findViewById(R.id.exo_content_frame);
        contentFrame.addView(videoProcessingGLSurfaceView);
        this.videoProcessingGLSurfaceView = videoProcessingGLSurfaceView;
        String dataSource = Objects.requireNonNull(params.get("dataSource")).toString();
        Object headers = params.get("httpHeaders");
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        Map<?, ?> httpHeaders = (Map<?, ?>) headers;
        List<Integer> breaks = new ArrayList<>();
        try {
            Object adBreaks = params.get("ad_breaks");
            if (adBreaks != null) {
                List<?> ls = convertObjectToList(adBreaks);
                if (ls != null) {
                    for (Object pos : ls) {
                        if (pos instanceof Integer) {
                            breaks.add((Integer) pos);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializePlayer(context, dataSource, httpHeaders, breaks);
    }

    private static List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    private void initializeChannel(BinaryMessenger binaryMessenger) {
        channel = new MethodChannel(
                binaryMessenger,
                "flutter.io/videoPlayer/adsVideoPlayerEvents" + viewId);
        channel.setMethodCallHandler(
                (caller, result) -> {
//                    Log.d("FlutterCall", caller.method + ": " + caller.arguments);
                    switch (caller.method) {
                        case "action::getPosition":
                            result.success(getPosition());
                            break;
                        case "action::seekTo":
                            seekTo((Integer) caller.arguments);
                            result.success(null);
                            break;
                        case "action::setSubtitleOption":
                            try {
                                Map<?, ?> hash = (Map<?, ?>) caller.arguments;
                                int groupIndex = (int) hash.get("groupIndex");
                                int trackIndex = (int) hash.get("trackIndex");
                                setSubtitles(groupIndex, trackIndex);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            result.success(null);
                            break;
                        case "action::setVolume":
                            setVolume((Double) caller.arguments);
                            result.success(null);
                            break;
                        case "action::play":
                            play();
                            result.success(null);
                            break;
                        case "action::pause":
                            pause();
                            result.success(null);
                            break;
                        default:
                            result.success(null);
                            break;
                    }
                });

    }

    private void initializePlayer(Context context, String dataSource, Map<?, ?> httpHeaders, List<Integer> breaks) throws
            Exception {
        Uri contentUri = Uri.parse(dataSource);

        DataSource.Factory dataSourceFactory;

        if (isHTTP(contentUri)) {
            DefaultHttpDataSource.Factory httpDataSourceFactory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent("ExoPlayer_TV")
                            .setAllowCrossProtocolRedirects(true);

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                Map<String, String> headers = new HashMap<>();
                Set<?> keys = httpHeaders.keySet();
                for (Object key : keys) {
                    Object value = httpHeaders.get(key);
                    if (key instanceof String && value instanceof String) {
                        headers.put((String) key, (String) value);
                    }
                }
                httpDataSourceFactory.setDefaultRequestProperties(headers);
            }
            dataSourceFactory = httpDataSourceFactory;
        } else {
            throw new Exception("The DataSource Not Supported Yet.");
        }
        MediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory)
                        .setAdsLoaderProvider(unusedAdTagUri -> adsLoader)
                        .setAdViewProvider(playerView);

        trackSelector = new DefaultTrackSelector(context);
        exoPlayer = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        if (vastTagUrl == null || vastTagUrl.isEmpty()) {
            MediaItem mediaItem;
            if (breaks.isEmpty()) {
                mediaItem = new MediaItem.Builder()
                        .setUri(contentUri)
                        .build();
            } else {
                mediaItem = new MediaItem.Builder()
                        .setUri(contentUri)
                        .setAdTagUri(getAdTagUri())
                        .build();
            }
            exoPlayer.setMediaItem(mediaItem);
        } else {
            if (breaks.isEmpty() || breaks.size() == 1) {
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(contentUri)
                        .setAdTagUri(getAdTagUri())
                        .build();
                exoPlayer.setMediaItem(mediaItem);
            } else {
                long st = 0;
                Uri adTagUri = getAdTagUri();
                for (long pos : breaks) {
                    MediaItem ad = new MediaItem.Builder()
                            .setUri(contentUri)
                            .setClipStartPositionMs(st)
                            .setClipEndPositionMs(st + pos * 1000)
                            .setAdTagUri(adTagUri, pos)
                            .build();
                    st += pos * 1000;
                    exoPlayer.addMediaItem(ad);
                }
                MediaItem ad = new MediaItem.Builder()
                        .setUri(contentUri)
                        .setClipStartPositionMs(st)
                        .setClipEndPositionMs(C.TIME_END_OF_SOURCE)
                        .build();
                exoPlayer.addMediaItem(ad);
            }
        }


        exoPlayer.prepare();

        exoPlayer.setPlayWhenReady(false);
        exoPlayer.addTextOutput(cues -> {
            Log.d("native", "addTextOutput: " + cues.size());
            if (cues.size() > 0) {
                Cue cue = cues.get(0);
                if (cue.text == null || cue.text == "") {
                    return;
                }
                Map<String, Object> event = new HashMap<>();
                event.put("event", "subtitle");
                event.put("values", cue.text.toString());
                eventSinkSuccess(event);
            }
        });

        exoPlayer.addMetadataOutput(metadata -> {
//            Log.d("native", "addMetadataOutput: " + metadata.toString());
            for (int i = 0; i < metadata.length(); i++) {
                Metadata.Entry entry = metadata.get(i);
                if (entry instanceof TextInformationFrame) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("key", ((TextInformationFrame) entry).value.split("_")[0]);
                    map.put("meta", ((TextInformationFrame) entry).value);
                    map.put("is_ad", isMediaMetadataAd(((TextInformationFrame) entry).value));
                    Map<String, Object> event = new HashMap<>();
                    event.put("event", "mediaMetadataChanged");
                    event.put("values", map);
                    eventSinkSuccess(event);
                }
            }
        });
        setupVideoPlayer();
    }

    private void setupVideoPlayer() {
        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView =
                Assertions.checkNotNull(this.videoProcessingGLSurfaceView);
        videoProcessingGLSurfaceView.setPlayer(exoPlayer);
        Assertions.checkNotNull(playerView).setPlayer(exoPlayer);
        adsLoader.setPlayer(exoPlayer);
        setAudioAttributes(exoPlayer);

        exoPlayer.addListener(this);
    }

    private boolean isMediaMetadataAd(String mediaDataValue) {
        try {
            String id = mediaDataValue.split("_")[0];
            if (exoPlayer != null && exoPlayer.getCurrentManifest() instanceof HlsManifest) {
                HlsManifest manifest = (HlsManifest) exoPlayer.getCurrentManifest();
                for (HlsMediaPlaylist.Segment seg : manifest.mediaPlaylist.segments) {
                    if (seg.url.contains(id) && seg.url.contains("is_ad=1")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setAudioAttributes(SimpleExoPlayer exoPlayer) {
        exoPlayer.setAudioAttributes(
                new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).build(),
                false);
    }

    void sendBufferingUpdate() {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "bufferingUpdate");
        List<? extends Number> range = Arrays.asList(0, exoPlayer.getBufferedPosition());
        event.put("values", Collections.singletonList(range));
        eventSinkSuccess(event);
    }

    private void sendInitialized() {
        if (isInitialized) {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "initialized");
            event.put("duration", exoPlayer.getDuration());
            if (exoPlayer.getVideoFormat() != null) {
                Format videoFormat = exoPlayer.getVideoFormat();
                int rotationDegrees = videoFormat.rotationDegrees;
                if (rotationDegrees == 90 || rotationDegrees == 270) {
                    event.put("width", exoPlayer.getVideoFormat().height);
                    event.put("height", exoPlayer.getVideoFormat().width);
                } else {
                    event.put("width", videoFormat.width);
                    event.put("height", videoFormat.height);
                }
            }
            eventSinkSuccess(event);
        }
    }

    private void getSubtitles() {
        List<Map<?, ?>> rawSubtitleItems = new ArrayList<>();
        TrackGroupArray trackGroups;
        int rendererIndex = 2;
        MappingTrackSelector.MappedTrackInfo trackInfo =
                trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
        if (trackSelector == null || trackInfo == null) {
            return;
        }
        trackGroups = trackInfo.getTrackGroups(rendererIndex);
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {

                if (group.getFormat(trackIndex).language != null && group.getFormat(trackIndex).label != null) {
                    Map<String, Object> raw = new HashMap<>();
                    raw.put("language", group.getFormat(trackIndex).language);
                    raw.put("label", group.getFormat(trackIndex).label);
                    raw.put("trackIndex", trackIndex);
                    raw.put("groupIndex", groupIndex);
                    raw.put("renderIndex", rendererIndex);
                    rawSubtitleItems.add(raw);
                }

            }
        }
        Map<String, Object> event = new HashMap<>();
        event.put("event", "subtitleList");
        event.put("values", rawSubtitleItems);
        eventSinkSuccess(event);
    }

    void setSubtitles(int groupIndex, int trackIndex) {
        boolean isDisabled;
        TrackGroupArray trackGroups;
        int rendererIndex = 2;
        DefaultTrackSelector.SelectionOverride override;

        MappingTrackSelector.MappedTrackInfo trackInfo =
                trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
        if (trackSelector == null || trackInfo == null) {
            return;
        }
        trackGroups = trackInfo.getTrackGroups(rendererIndex);
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        isDisabled = parameters.getRendererDisabled(rendererIndex);
        DefaultTrackSelector.ParametersBuilder parametersBuilder = trackSelector.buildUponParameters();
        parametersBuilder.setRendererDisabled(rendererIndex, isDisabled);
        override = new DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex);
        parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override);
        trackSelector.setParameters(parametersBuilder);
    }

    long getPosition() {
        if (exoPlayer != null) {
            return exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    void seekTo(int location) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(location);
        }
    }

    void play() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    void pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    private void releasePlayer() {
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
            adsLoader.release();
        }
        if (videoProcessingGLSurfaceView != null) {
            videoProcessingGLSurfaceView.setPlayer(null);
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        channel.setMethodCallHandler(null);
    }

    void setVolume(double value) {
        if (exoPlayer != null) {
            float bracketedValue = (float) Math.max(0.0, Math.min(1.0, value));
            exoPlayer.setVolume(bracketedValue);
        }
    }

    private static boolean isHTTP(Uri uri) {
        if (uri == null || uri.getScheme() == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme.equals("http") || scheme.equals("https");
    }

    private void eventSinkSuccess(Map<String, Object> values) {
        if (channel != null) {
            channel.invokeMethod("nativeCall", values);
        }
    }

    private boolean isBuffering = false;

    public void setBuffering(boolean buffering) {
        if (isBuffering != buffering) {
            isBuffering = buffering;
            Map<String, Object> event = new HashMap<>();
            event.put("event", isBuffering ? "bufferingStart" : "bufferingEnd");
            eventSinkSuccess(event);
        }
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        if (isLoading) {
            Map<String, Object> event = new HashMap<>();
            event.put("values", exoPlayer.getCurrentPosition());
            event.put("event", "playbackCurrentPosition");
            event.put("duration", exoPlayer.getDuration());
            eventSinkSuccess(event);
        }
    }

    @Override
    public void onPlaybackStateChanged(final int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            setBuffering(true);
            sendBufferingUpdate();
        } else if (playbackState == Player.STATE_READY) {
            if (!isInitialized) {
                isInitialized = true;
                sendInitialized();
            }
            getSubtitles();
        } else if (playbackState == Player.STATE_ENDED) {
            Map<String, Object> event = new HashMap<>();
            event.put("event", "completed");
            eventSinkSuccess(event);
        }

        if (playbackState != Player.STATE_BUFFERING) {
            setBuffering(false);
        }
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST) {
            long position = exoPlayer.getCurrentPosition();
            Map<String, Object> event = new HashMap<>();
            event.put("values", position);
            if (playWhenReady) {
                event.put("event", "playbackPlay");
            } else {
                event.put("event", "playbackPause");
            }
            eventSinkSuccess(event);
        }
    }

    @Override
    public void onPlayerError(@NonNull final ExoPlaybackException error) {
        setBuffering(false);
        Map<String, Object> event = new HashMap<>();
        event.put("event", "playbackError");
        event.put("values", error.toString());
        eventSinkSuccess(event);
    }
}