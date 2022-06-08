// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

/// An example of using the plugin, controlling lifecycle and playback of the
/// video.
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:video_player/video_player.dart';

void mainNative() {
  runApp(
    Shortcuts(
      shortcuts: {
        LogicalKeySet(LogicalKeyboardKey.select): const ActivateIntent(),
      },
      child: MaterialApp(
        home: _App(),
      ),
    ),
  );
}

class _App extends StatefulWidget {
  @override
  State<_App> createState() => _AppState();
}

class _AppState extends State<_App> {
  final videoController = ValueNotifier<NativeVideoController?>(null);
  Key videoKey = ValueKey('video_key');
  @override
  void initState() {
    init();
    super.initState();
  }

  void init() async {
    await TTAnalytics.shared().init(key: '35B0FD19-C1A4-468A-B99F-14CAA5A3A2FD'); // chien srv
    // await TTAnalytics.shared().init(key: '4E4EC445-31FA-4A8F-9F96-8EE86A9CB190'); // owner test
    await TTAnalytics.shared().setConfig(TTAnalyticsConfig(
      videoId: 'playlist.m3u8',
      title: 'elephants dream ios 1',
      path: 'example/video_player',
      customerUserId: 'u-1234567',
    ));
  }

  @override
  void dispose() {
    videoController.dispose();
    super.dispose();
  }

  List<SubtitleOption> options = [];
  int subSelectIndex = -1;

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 1,
      child: Scaffold(
        key: const ValueKey<String>('home_page'),
        appBar: AppBar(
          actions: [
            ValueListenableBuilder<NativeVideoController?>(
              valueListenable: videoController,
              builder: (context, controller, _) {
                if (controller != null) {
                  return IconButton(
                    onPressed: () {
                      if (controller.value.isPlaying) {
                        controller.pause();
                      } else {
                        controller.play();
                      }
                    },
                    icon: Icon(controller.value.isPlaying ? Icons.pause : Icons.play_arrow),
                  );
                }
                return SizedBox();
              },
            ),
            ValueListenableBuilder<NativeVideoController?>(
              valueListenable: videoController,
              builder: (context, controller, _) {
                if (controller != null) {
                  return ChangeNotifierProvider.value(
                    value: controller,
                    child: Consumer<NativeVideoController>(builder: ((context, value, child) {
                      if (options.isEmpty && controller.value.subtitleList.isNotEmpty) {
                        options.addAll(controller.value.subtitleList);
                      }
                      if (options.isNotEmpty) {
                        return IconButton(
                          onPressed: () {
                            if (subSelectIndex + 1 < options.length) {
                              subSelectIndex += 1;
                              controller.setSubtitleOption(options[subSelectIndex]);
                            } else {
                              subSelectIndex = -1;
                              controller.setSubtitleOption(SubtitleOption.none());
                            }
                          },
                          icon: Icon(Icons.subtitles),
                        );
                      }
                      return SizedBox();
                    })),
                  );
                }
                return SizedBox();
              },
            )
          ],
        ),
        body: Stack(
          alignment: Alignment.center,
          children: [
            Positioned.fill(
              child: NativeVideoPlayer(
                key: videoKey,
                dataSource: 'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
                onCreated: (controller) {
                  videoController.value = controller;
                },
              ),
            ),
            Positioned(
              bottom: 100,
              child: ValueListenableBuilder<NativeVideoController?>(
                valueListenable: videoController,
                builder: (context, controller, _) {
                  if (controller != null) {
                    return ChangeNotifierProvider.value(
                      value: controller,
                      child: Consumer<NativeVideoController>(builder: ((context, value, child) {
                        return Text(value.value.caption.text);
                      })),
                    );
                  }
                  return SizedBox();
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
