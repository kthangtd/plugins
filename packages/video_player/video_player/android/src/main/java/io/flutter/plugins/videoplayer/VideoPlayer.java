// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.Listener;
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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

final class VideoPlayer {
    private static final String FORMAT_SS = "ss";
    private static final String FORMAT_DASH = "dash";
    private static final String FORMAT_HLS = "hls";
    private static final String FORMAT_OTHER = "other";

    private SimpleExoPlayer exoPlayer;

    private Surface surface;

    private final TextureRegistry.SurfaceTextureEntry textureEntry;

    private QueuingEventSink eventSink = new QueuingEventSink();

    private final EventChannel eventChannel;

    private boolean isInitialized = false;

    private final VideoPlayerOptions options;

    private DefaultTrackSelector trackSelector;

    VideoPlayer(
            Context context,
            EventChannel eventChannel,
            TextureRegistry.SurfaceTextureEntry textureEntry,
            String dataSource,
            String formatHint,
            Map<String, String> httpHeaders,
            VideoPlayerOptions options) throws Exception {
        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;
        this.options = options;

        trackSelector = new DefaultTrackSelector(context);
        exoPlayer = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build();

        Uri uri = Uri.parse(dataSource);

        DataSource.Factory dataSourceFactory;
        if (isHTTP(uri)) {
            DefaultHttpDataSource.Factory httpDataSourceFactory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent("ExoPlayer")
                            .setAllowCrossProtocolRedirects(true);

            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                httpDataSourceFactory.setDefaultRequestProperties(httpHeaders);
            }
            dataSourceFactory = httpDataSourceFactory;
        } else {
            throw new Exception("The DataSource Not Supported Yet.");
        }

        MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory, formatHint, context);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();

        exoPlayer.addTextOutput(cues -> {
            if (cues.size() > 0) {
                Cue cue = cues.get(0);
                if (cue.text == null || cue.text == "") {
                    return;
                }
                Map<String, Object> event = new HashMap<>();
                event.put("event", "subtitle");
                event.put("values", cue.text.toString());
                eventSink.success(event);
            }
        });

        exoPlayer.addMetadataOutput(metadata -> {
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
                    eventSink.success(event);
                }
            }
        });

        setupVideoPlayer(eventChannel, textureEntry);
    }

    private boolean isMediaMetadataAd(String mediaDataValue) {
        try {
            String id = mediaDataValue.split("_")[0];
            if (exoPlayer != null && exoPlayer.getCurrentManifest() instanceof HlsManifest) {
                HlsManifest manifest = (HlsManifest) exoPlayer.getCurrentManifest();
                if (manifest.mediaPlaylist != null && manifest.mediaPlaylist.segments != null) {
                    for (HlsMediaPlaylist.Segment seg : manifest.mediaPlaylist.segments) {
                        if (seg != null && seg.url != null && seg.url.contains(id) && seg.url.contains("is_ad=1")) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isHTTP(Uri uri) {
        if (uri == null || uri.getScheme() == null) {
            return false;
        }
        String scheme = uri.getScheme();
        return scheme.equals("http") || scheme.equals("https");
    }

    private MediaSource buildMediaSource(
            Uri uri, DataSource.Factory mediaDataSourceFactory, String formatHint, Context context) {
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

    private void setupVideoPlayer(
            EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry) {
        eventChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink sink) {
                        eventSink.setDelegate(sink);
                    }

                    @Override
                    public void onCancel(Object o) {
                        eventSink.setDelegate(null);
                    }
                });

        surface = new Surface(textureEntry.surfaceTexture());
        exoPlayer.setVideoSurface(surface);
        setAudioAttributes(exoPlayer, options.mixWithOthers);

        exoPlayer.addListener(
                new Listener() {
                    private boolean isBuffering = false;

                    public void setBuffering(boolean buffering) {
                        if (isBuffering != buffering) {
                            isBuffering = buffering;
                            Map<String, Object> event = new HashMap<>();
                            event.put("event", isBuffering ? "bufferingStart" : "bufferingEnd");
                            eventSink.success(event);
                        }
                    }

                    @Override
                    public void onIsLoadingChanged(boolean isLoading) {
                        if (isLoading) {
                            Map<String, Object> event = new HashMap<>();
                            event.put("values", exoPlayer.getCurrentPosition());
                            event.put("event", "playbackCurrentPosition");
                            eventSink.success(event);
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
                            eventSink.success(event);
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
                            eventSink.success(event);
                        }
                    }


                    @Override
                    public void onPlayerError(@NonNull final ExoPlaybackException error) {
                        setBuffering(false);
                        if (eventSink != null) {
                            Map<String, Object> event = new HashMap<>();
                            event.put("event", "playbackError");
                            event.put("values", error.toString());
                            eventSink.success(event);
                            eventSink.error("VideoError", "Video player had error " + error, null);

                        }
                    }
                });
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
        eventSink.success(event);
    }

    void setSubtitles(Messages.SubtitleMessage arg) {
        boolean isDisabled;
        TrackGroupArray trackGroups;
        int rendererIndex = 2;
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
        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(arg.getSubtitleGroupIndex());
        parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override);
//        TrackSelectionOverrides.TrackSelectionOverride override =
//                new TrackSelectionOverrides.TrackSelectionOverride(trackGroups.get(arg.getSubtitleGroupIndex()));
//        parametersBuilder.setTrackSelectionOverrides(
//                new TrackSelectionOverrides.Builder().addOverride(override).build());
        trackSelector.setParameters(parametersBuilder);
    }

    void sendBufferingUpdate() {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "bufferingUpdate");
        List<? extends Number> range = Arrays.asList(0, exoPlayer.getBufferedPosition());
        // iOS supports a list of buffered ranges, so here is a list with a single range.
        event.put("values", Collections.singletonList(range));
        eventSink.success(event);
    }

    @SuppressWarnings("deprecation")
    private static void setAudioAttributes(SimpleExoPlayer exoPlayer, boolean isMixMode) {
        exoPlayer.setAudioAttributes(
                new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).build(), !isMixMode);
    }

    void play() {
        exoPlayer.setPlayWhenReady(true);
    }

    void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    void setLooping(boolean value) {
        exoPlayer.setRepeatMode(value ? REPEAT_MODE_ALL : REPEAT_MODE_OFF);
    }

    void setVolume(double value) {
        float bracketedValue = (float) Math.max(0.0, Math.min(1.0, value));
        exoPlayer.setVolume(bracketedValue);
    }

    void setPlaybackSpeed(double value) {
        // We do not need to consider pitch and skipSilence for now as we do not handle them and
        // therefore never diverge from the default values.
        final PlaybackParameters playbackParameters = new PlaybackParameters(((float) value));

        exoPlayer.setPlaybackParameters(playbackParameters);
    }

    void seekTo(int location) {
        exoPlayer.seekTo(location);
    }

    long getPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @SuppressWarnings("SuspiciousNameCombination")
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
            eventSink.success(event);
        }
    }

    void dispose() {
        if (isInitialized) {
            exoPlayer.stop();
        }
        textureEntry.release();
        eventChannel.setStreamHandler(null);
        if (surface != null) {
            surface.release();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
