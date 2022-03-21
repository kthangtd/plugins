// ignore_for_file: public_member_api_docs

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';

class NativeVideoController extends ValueNotifier<VideoPlayerValue> {
  final String dataSource;
  final Map<String, String> httpHeaders;
  final int viewId;

  late MethodChannel _channel;
  bool _isDisposed = false;
  Timer? _timer;

  final customValue = ValueNotifier({});

  NativeVideoController(
    this.dataSource, {
    this.httpHeaders = const {},
    required this.viewId,
  }) : super(VideoPlayerValue(duration: Duration.zero)) {
    _channel = MethodChannel('flutter.io/videoPlayer/nativeVideoEvents$viewId');
    _channel.setMethodCallHandler(_onPlatformCall);
  }

  Future _onPlatformCall(MethodCall caller) async {
    print('FlutterCall: [${caller.method}]: ${caller.arguments}');
    final map = caller.arguments as Map;
    Future.delayed(Duration(milliseconds: 1), () => _onEvent(map));
    return null;
  }

  void _onEvent(Map map) {
    final event = map['event'] ?? '';
    switch (event) {
      case 'initialized':
        final d = map['duration'];
        value = value.copyWith(
          duration: Duration(milliseconds: d ?? 0),
          size: Size((map['width'] ?? 0) * 1.0, (map['height'] ?? 0) * 1.0),
          isInitialized: d != null,
        );
        try {
          _applyVolume();
          _applyPlayPause();
        } catch (_) {}
        break;
      case 'completed':
        pause().then((_) => seekTo(value.duration));
        customValue.value = {"event": "playbackEnded"};
        break;
      case 'subtitleList':
        final values = map['values'] ?? [];
        final List<SubtitleOption> ls = values.map<SubtitleOption>((e) => SubtitleOption(e)).toList();
        value = value.copyWith(subtitleList: ls);
        break;
      case 'subtitle':
        value = value.copyWith(
          caption: Caption(
            number: 0,
            start: Duration.zero,
            end: Duration.zero,
            text: map['values'] ?? '',
          ),
        );
        break;
      case 'playbackPlay':
      case 'playbackPause':
      case 'playbackError':
      case 'mediaMetadataChanged':
      case 'playbackCurrentPosition':
        customValue.value = map;
        break;
      default:
    }
  }

  /// The position in the current video.
  Future<Duration?> get position async {
    if (_isDisposed) {
      return null;
    }
    try {
      final p = await _channel.invokeMethod('action::getPosition');
      if (p is int) {
        return Duration(milliseconds: p);
      }
    } catch (_) {}
    return null;
  }

  bool get _isDisposedOrNotInitialized => _isDisposed || !value.isInitialized;

  /// Starts playing the video.
  ///
  /// If the video is at the end, this method starts playing from the beginning.
  ///
  /// This method returns a future that completes as soon as the "play" command
  /// has been sent to the platform, not when playback itself is totally
  /// finished.
  Future<void> play() async {
    if (value.position == value.duration) {
      await seekTo(const Duration());
    }
    value = value.copyWith(isPlaying: true);
    await _applyPlayPause();
  }

  /// Pauses the video.
  Future<void> pause() async {
    value = value.copyWith(isPlaying: false);
    await _applyPlayPause();
  }

  /// Sets the video's current timestamp to be at [moment]. The next
  /// time the video is played it will resume from the given [moment].
  ///
  /// If [moment] is outside of the video's full range it will be automatically
  /// and silently clamped.
  Future<void> seekTo(Duration position) async {
    if (_isDisposedOrNotInitialized) {
      return;
    }
    if (position > value.duration) {
      position = value.duration;
    } else if (position < const Duration()) {
      position = const Duration();
    }
    await _channel.invokeMethod('action::seekTo', position.inMilliseconds);
    _updatePosition(position);
  }

  /// Sets the audio volume of [this].
  ///
  /// [volume] indicates a value between 0.0 (silent) and 1.0 (full volume) on a
  /// linear scale.
  Future<void> setVolume(double volume) async {
    value = value.copyWith(volume: volume.clamp(0.0, 1.0));
    await _applyVolume();
  }

  /// Sets the subtitle of [this].
  ///
  ///
  Future<void> setSubtitleOption(SubtitleOption option) async {
    if (option != null && !_isDisposedOrNotInitialized) {
      await _channel.invokeMethod('action::setSubtitleOption', {
        'groupIndex': option.groupIndex,
        'trackIndex': option.trackIndex,
      });
    }
  }

  Future<void> _applyVolume() async {
    if (_isDisposedOrNotInitialized) {
      return;
    }
    await _channel.invokeMethod('action::setVolume', value.volume);
  }

  Future<void> _applyPlayPause() async {
    if (_isDisposedOrNotInitialized) {
      return;
    }
    if (value.isPlaying) {
      await _channel.invokeMethod('action::play');
      // Cancel previous timer.
      _timer?.cancel();
      _timer = Timer.periodic(
        const Duration(milliseconds: 500),
        (Timer timer) async {
          if (_isDisposed) {
            return;
          }
          final Duration? newPosition = await position;
          if (newPosition == null) {
            return;
          }
          _updatePosition(newPosition);
        },
      );
    } else {
      _timer?.cancel();
      await _channel.invokeMethod('action::pause');
    }
  }

  void _updatePosition(Duration position) {
    if (!_isDisposed) {
      value = value.copyWith(position: position);
    }
  }

  @override
  void removeListener(VoidCallback listener) {
    // Prevent VideoPlayer from causing an exception to be thrown when attempting to
    // remove its own listener after the controller has already been disposed.
    if (!_isDisposed) {
      super.removeListener(listener);
    }
  }

  void release() {
    if (!_isDisposed) {
      _isDisposed = true;
      _timer?.cancel();
    }
    _channel.setMethodCallHandler(null);
    _isDisposed = true;
  }

  @override
  Future<void> dispose() async {
    release();
    super.dispose();
  }
}
