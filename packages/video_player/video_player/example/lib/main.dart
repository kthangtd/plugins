// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs, unused_element

/// An example of using the plugin, controlling lifecycle and playback of the
/// video.

import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

void main() {
  runApp(
    MaterialApp(
      home: _App(),
    ),
  );
}

class _App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 1,
      child: Scaffold(
        key: const ValueKey<String>('home_page'),
        appBar: AppBar(
          title: const Text('Video player example'),
          actions: <Widget>[],
          bottom: const TabBar(
            isScrollable: true,
            tabs: <Widget>[
              Tab(
                icon: Icon(Icons.cloud),
                text: "Remote",
              ),
            ],
          ),
        ),
        body: TabBarView(
          children: <Widget>[
            _BumbleBeeRemoteVideo(),
          ],
        ),
      ),
    );
  }
}

class _BumbleBeeRemoteVideo extends StatefulWidget {
  @override
  _BumbleBeeRemoteVideoState createState() => _BumbleBeeRemoteVideoState();
}

class _BumbleBeeRemoteVideoState extends State<_BumbleBeeRemoteVideo> {
  VideoPlayerController? _controller;

  final subtitleOptions = <SubtitleOption>[];

  @override
  void initState() {
    super.initState();
    init();
  }

  void init() async {
    await TTAnalytics.shared().init(key: '35B0FD19-C1A4-468A-B99F-14CAA5A3A2FD'); // chien srv
    // await TTAnalytics.shared().init(key: '4E4EC445-31FA-4A8F-9F96-8EE86A9CB190'); // owner test
    await TTAnalytics.shared().setConfig(
      videoId: 'playlist.m3u8',
      videoTitle: 'elephants dream 3',
      videoUrl: 'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
      userId: 'u-1234567',
    );
    _controller = VideoPlayerController.network(
      'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
      // closedCaptionFile: _loadCaptions(),
      videoPlayerOptions: VideoPlayerOptions(mixWithOthers: true),
    );

    _controller!.addListener(() {
      setState(() {});
    });
    _controller!.setLooping(true);
    _controller!.initialize();
    _controller!.addListener(onSubtitleOption);
    _controller!.addListener(() {
      print('subtitle: ${_controller!.value.caption.text}');
    });
  }

  void onSubtitleOption() {
    final _controller = this._controller;
    if (_controller != null) {
      subtitleOptions.clear();
      subtitleOptions.addAll(_controller.value.subtitleList);
      if (subtitleOptions.isNotEmpty) {
        _controller.removeListener(onSubtitleOption);
        setState(() {});
      }
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final _controller = this._controller;
    if (_controller == null) {
      return SizedBox();
    }
    return SingleChildScrollView(
      child: Column(
        children: <Widget>[
          Container(padding: const EdgeInsets.only(top: 20.0)),
          const Text('With remote mp4'),
          Container(
            padding: const EdgeInsets.all(20),
            child: AspectRatio(
              aspectRatio: _controller.value.aspectRatio,
              child: Stack(
                alignment: Alignment.bottomCenter,
                children: <Widget>[
                  VideoPlayer(_controller),
                  // ClosedCaption(text: _controller.value.caption.text),
                  _ControlsOverlay(controller: _controller),
                  VideoProgressIndicator(_controller, allowScrubbing: true),
                ],
              ),
            ),
          ),
          if (subtitleOptions.isNotEmpty)
            Column(
              children: subtitleOptions.map((e) {
                return TextButton(
                  onPressed: () {
                    _controller.setSubtitleOption(e);
                  },
                  child: Text(e.label),
                );
              }).toList()
                ..add(TextButton(
                  onPressed: () {
                    final index = Platform.isIOS ? -1 : 0;
                    _controller.setSubtitleOption(SubtitleOption({
                      'trackIndex': index,
                      'groupIndex': index,
                    }));
                  },
                  child: Text('None'),
                )),
            )
        ],
      ),
    );
  }
}

class _ControlsOverlay extends StatelessWidget {
  const _ControlsOverlay({Key? key, required this.controller}) : super(key: key);

  static const _examplePlaybackRates = [
    0.25,
    0.5,
    1.0,
    1.5,
    2.0,
    3.0,
    5.0,
    10.0,
  ];

  final VideoPlayerController controller;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        AnimatedSwitcher(
          duration: Duration(milliseconds: 50),
          reverseDuration: Duration(milliseconds: 200),
          child: controller.value.isPlaying
              ? SizedBox.shrink()
              : Container(
                  color: Colors.black26,
                  child: Center(
                    child: Icon(
                      Icons.play_arrow,
                      color: Colors.white,
                      size: 100.0,
                    ),
                  ),
                ),
        ),
        GestureDetector(
          onTap: () {
            controller.value.isPlaying ? controller.pause() : controller.play();
          },
        ),
        Align(
          alignment: Alignment.topRight,
          child: PopupMenuButton<double>(
            initialValue: controller.value.playbackSpeed,
            tooltip: 'Playback speed',
            onSelected: (speed) {
              controller.setPlaybackSpeed(speed);
            },
            itemBuilder: (context) {
              return [
                for (final speed in _examplePlaybackRates)
                  PopupMenuItem(
                    value: speed,
                    child: Text('${speed}x'),
                  )
              ];
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(
                // Using less vertical padding as the text is also longer
                // horizontally, so it feels like it would need more spacing
                // horizontally (matching the aspect ratio of the video).
                vertical: 12,
                horizontal: 16,
              ),
              child: Text('${controller.value.playbackSpeed}x'),
            ),
          ),
        ),
      ],
    );
  }
}
