package io.flutter.plugins.videoplayer.widget;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugins.videoplayer.R;

public class TTNativeVideoPlayer implements PlatformView {
    private static final String FORMAT_HLS = "hls";

    @Nullable
    private PlayerView playerView;

    @Nullable
    private VideoProcessingGLSurfaceView videoProcessingGLSurfaceView;

    private SimpleExoPlayer exoPlayer;
    private MethodChannel channel;
    private boolean isInitialized = false;
    private DefaultTrackSelector trackSelector;
    private int viewId;
    private Handler handler;

    @Override
    public View getView() {
        return videoProcessingGLSurfaceView;
    }

    @Override
    public void dispose() {
        if (playerView != null) {
            playerView.onPause();
        }
        releasePlayer();
    }

    public TTNativeVideoPlayer(Context context, int id, HashMap params,
                               BinaryMessenger binaryMessenger) throws Exception {
        handler = new Handler(Looper.myLooper());
        this.viewId = id;
        initializeChannel(binaryMessenger);
        playerView = new PlayerView(context);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setUseController(false);
        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView =
                new VideoProcessingGLSurfaceView(
                        context, false, new BitmapOverlayVideoProcessor(context));
        FrameLayout contentFrame = playerView.findViewById(R.id.exo_content_frame);
        contentFrame.addView(videoProcessingGLSurfaceView);
        this.videoProcessingGLSurfaceView = videoProcessingGLSurfaceView;
        String dataSource = Objects.requireNonNull(params.get("dataSource")).toString();
        Object httpHeaders = params.get("httpHeaders");
        initializePlayer(context, dataSource, (Map) httpHeaders);
    }

    private void initializeChannel(BinaryMessenger binaryMessenger) {
        channel = new MethodChannel(
                binaryMessenger,
                "flutter.io/videoPlayer/nativeVideoEvents" + viewId);
        channel.setMethodCallHandler(
                (caller, result) -> {
                    Log.d("FlutterCall", caller.method + ": " + caller.arguments);
                    switch (caller.method) {
                        case "action::getPosition":
                            result.success(getPosition());
                            break;
                        case "action::seekTo":
                            seekTo((Integer) caller.arguments);
                            result.success(null);
                            break;
                        case "action::setSubtitleOption":
                            HashMap hash = (HashMap) caller.arguments;
                            int groupIndex = (int) hash.get("groupIndex");
                            int trackIndex = (int) hash.get("trackIndex");
                            setSubtitles(groupIndex, trackIndex);
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

    private void initializePlayer(Context context, String dataSource, Map httpHeaders) throws
            Exception {

        trackSelector = new DefaultTrackSelector(context);
        exoPlayer = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build();

        Uri uri = Uri.parse(dataSource);

        DataSource.Factory dataSourceFactory;
        if (isHTTP(uri)) {
            DefaultHttpDataSource.Factory httpDataSourceFactory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent("ExoPlayer_TV")
                            .setAllowCrossProtocolRedirects(true);

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                httpDataSourceFactory.setDefaultRequestProperties(httpHeaders);
            }
            dataSourceFactory = httpDataSourceFactory;
        } else {
            throw new Exception("The DataSource Not Supported Yet.");
        }

        MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory, FORMAT_HLS);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
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
            Log.d("native", "addMetadataOutput: " + metadata.toString());
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

    private MediaSource buildMediaSource(Uri uri, DataSource.Factory
            mediaDataSourceFactory, String formatHint) {
        int type;
        if (formatHint == null) {
            type = Util.inferContentType(uri.getLastPathSegment());
        } else {
            if (FORMAT_HLS.equals(formatHint)) {
                type = C.TYPE_HLS;
            } else {
                type = -1;
            }
        }
        if (type == C.TYPE_HLS) {
            return new HlsMediaSource.Factory(mediaDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private void setupVideoPlayer() {
        VideoProcessingGLSurfaceView videoProcessingGLSurfaceView =
                Assertions.checkNotNull(this.videoProcessingGLSurfaceView);
        videoProcessingGLSurfaceView.setPlayer(exoPlayer);
        Assertions.checkNotNull(playerView).setPlayer(exoPlayer);
//        exoPlayer.addAnalyticsListener(new EventLogger(/* trackSelector= */ null));
        setAudioAttributes(exoPlayer, true);

        exoPlayer.addListener(
                new Player.Listener() {
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
                });
    }

    private boolean isMediaMetadataAd(String mediaDataValue) {
        String id = mediaDataValue.split("_")[0];
        if (exoPlayer.getCurrentManifest() instanceof HlsManifest) {
            HlsManifest manifest = (HlsManifest) exoPlayer.getCurrentManifest();
            for (HlsMediaPlaylist.Segment seg : manifest.mediaPlaylist.segments) {
                if (seg.url.contains(id) && seg.url.contains("is_ad=1")) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static void setAudioAttributes(SimpleExoPlayer exoPlayer, boolean isMixMode) {
        exoPlayer.setAudioAttributes(
                new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).build(), !isMixMode);
    }

    void sendBufferingUpdate() {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "bufferingUpdate");
        List<? extends Number> range = Arrays.asList(0, exoPlayer.getBufferedPosition());
        // iOS supports a list of buffered ranges, so here is a list with a single range.
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
                int width = videoFormat.width;
                int height = videoFormat.height;
                int rotationDegrees = videoFormat.rotationDegrees;
                // Switch the width/height if video was taken in portrait mode
                if (rotationDegrees == 90 || rotationDegrees == 270) {
                    width = exoPlayer.getVideoFormat().height;
                    height = exoPlayer.getVideoFormat().width;
                }
                event.put("width", width);
                event.put("height", height);
            }
            eventSinkSuccess(event);
        }
    }

    private void getSubtitles() {
        List<Map<?, ?>> rawSubtitleItems = new ArrayList<Map<?, ?>>();
        TrackGroupArray trackGroups;
        int rendererIndex = 2;
        DefaultTrackSelector.SelectionOverride override;

        MappingTrackSelector.MappedTrackInfo trackInfo =
                trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
        if (trackSelector == null || trackInfo == null) {
            // TrackSelector not initialized
            return;
        }

        trackGroups = trackInfo.getTrackGroups(rendererIndex);
        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();

        // Add per-track views.
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {

                if (group.getFormat(trackIndex).language != null && group.getFormat(trackIndex).label != null) {
                    Map<String, Object> raw = new HashMap<String, Object>();
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
            // TrackSelector not initialized
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
        return exoPlayer.getCurrentPosition();
    }

    void seekTo(int location) {
        exoPlayer.seekTo(location);
    }

    void play() {
        exoPlayer.setPlayWhenReady(true);
    }

    void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    private void releasePlayer() {
        if (playerView != null) {
            playerView.setPlayer(null);
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
        float bracketedValue = (float) Math.max(0.0, Math.min(1.0, value));
        exoPlayer.setVolume(bracketedValue);
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
}