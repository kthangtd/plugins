// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

/// An example of using the plugin, controlling lifecycle and playback of the
/// video.

import 'dart:io';

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
          'https://pbs.getpublica.com/v1/s2s-hb?app_bundle=com.samsungtvplus.butacatv&did=5a569289-f54b-048c-a3ee-8d4d4fc028e0&app_name=butaca.tvplus&app_store_url=https%3A%2F%2Fwww.samsung.com%2Fus%2Fappstore%2Fapp.do%3FappId%3DG15147002586&format=vast&cb=4486853104188&ip=187.144.75.191&ua=Mozilla%2F5.0+%28SMART-TV%3B+Linux%3B+Tizen+5.5%29+AppleWebKit%2F538.1+%28KHTML%2C+like+Gecko%29+Version%2F5.5+TV+Safari%2F538.1&player_height=1080&player_width=1920&is_lat=1&content_title=Bellas+de+Noche&device_type=CTV&site_id=11106&pod_duration=120&content_id=cin01268&content_genre=drama&max_ad_duration=30&min_ad_duration=6&content_length=5400&livestream=1&content_prodqual=1&content_series=',
      adBreaks: [5, 10, 15, 20, 25],
    );
    super.initState();
    videoController.value = controller;
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
