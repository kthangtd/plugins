// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

/// An example of using the plugin, controlling lifecycle and playback of the
/// video.

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:video_player/video_player.dart' show AdsVideoController, AdsVideoPlayer, SubtitleOption;

void main() {
  runApp(
    MaterialApp(
      home: _App(),
    ),
  );
}

class _App extends StatefulWidget {
  @override
  State<_App> createState() => _AppState();
}

class _AppState extends State<_App> {
  final videoController = ValueNotifier<AdsVideoController?>(null);
  Key videoKey = ValueKey('video_key');
  late AdsVideoController controller;

  @override
  void initState() {
    controller = AdsVideoController(
      'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
      vastTagUrl:
          'https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpreonly&ciu_szs=300x250%2C728x90&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&correlator=',
      adBreaks: [5, 10, 20],
    );
    super.initState();
    videoController.value = controller;
    Future.delayed(Duration(seconds: 2), () {
      controller.play();
    });
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
        appBar: AppBar(
          title: Text('Ads Video Player'),
          actions: [
            ValueListenableBuilder<AdsVideoController?>(
              valueListenable: videoController,
              builder: (context, controller, _) {
                if (controller != null) {
                  return ChangeNotifierProvider.value(
                    value: controller,
                    child: Consumer<AdsVideoController>(
                      builder: ((context, controller, child) {
                        if (!controller.value.isAdPlaying) {
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
                      }),
                    ),
                  );
                }
                return SizedBox();
              },
            ),
            ValueListenableBuilder<AdsVideoController?>(
              valueListenable: videoController,
              builder: (context, controller, _) {
                if (controller != null) {
                  return ChangeNotifierProvider.value(
                    value: controller,
                    child: Consumer<AdsVideoController>(builder: ((context, value, child) {
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
              child: AdsVideoPlayer(
                key: videoKey,
                controller: controller,
              ),
            ),
            Positioned(
              bottom: 100,
              child: ValueListenableBuilder<AdsVideoController?>(
                valueListenable: videoController,
                builder: (context, controller, _) {
                  if (controller != null) {
                    return ChangeNotifierProvider.value(
                      value: controller,
                      child: Consumer<AdsVideoController>(builder: ((context, value, child) {
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
