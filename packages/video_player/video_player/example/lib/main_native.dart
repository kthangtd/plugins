// // Copyright 2013 The Flutter Authors. All rights reserved.
// // Use of this source code is governed by a BSD-style license that can be
// // found in the LICENSE file.

// // ignore_for_file: public_member_api_docs

// /// An example of using the plugin, controlling lifecycle and playback of the
// /// video.

// import 'dart:io';

// import 'package:flutter/cupertino.dart';
// import 'package:flutter/material.dart';
// import 'package:provider/provider.dart';
// import 'package:video_player/video_player.dart';

// void main() {
//   runApp(
//     MaterialApp(
//       home: _App(),
//     ),
//   );
// }

// class _App extends StatefulWidget {
//   @override
//   State<_App> createState() => _AppState();
// }

// class _AppState extends State<_App> {
//   final videoController = ValueNotifier<NativeVideoController?>(null);
//   Key videoKey = ValueKey('video_key');
//   @override
//   void dispose() {
//     videoController.dispose();
//     super.dispose();
//   }

//   List<SubtitleOption> options = [];
//   int subSelectIndex = -1;

//   @override
//   Widget build(BuildContext context) {
//     return DefaultTabController(
//       length: 1,
//       child: Scaffold(
//         key: const ValueKey<String>('home_page'),
//         appBar: AppBar(
//           actions: [
//             ValueListenableBuilder<NativeVideoController?>(
//               valueListenable: videoController,
//               builder: (context, controller, _) {
//                 if (controller != null) {
//                   return IconButton(
//                     onPressed: () {
//                       if (controller.value.isPlaying) {
//                         controller.pause();
//                       } else {
//                         controller.play();
//                       }
//                     },
//                     icon: Icon(controller.value.isPlaying ? Icons.pause : Icons.play_arrow),
//                   );
//                 }
//                 return SizedBox();
//               },
//             ),
//             ValueListenableBuilder<NativeVideoController?>(
//               valueListenable: videoController,
//               builder: (context, controller, _) {
//                 if (controller != null) {
//                   return ChangeNotifierProvider.value(
//                     value: controller,
//                     child: Consumer<NativeVideoController>(builder: ((context, value, child) {
//                       if (options.isEmpty && controller.value.subtitleList.isNotEmpty) {
//                         options.addAll(controller.value.subtitleList);
//                       }
//                       if (options.isNotEmpty) {
//                         return IconButton(
//                           onPressed: () {
//                             if (subSelectIndex + 1 < options.length) {
//                               subSelectIndex += 1;
//                               controller.setSubtitleOption(options[subSelectIndex]);
//                             } else {
//                               subSelectIndex = -1;
//                               controller.setSubtitleOption(SubtitleOption.none());
//                             }
//                           },
//                           icon: Icon(Icons.subtitles),
//                         );
//                       }
//                       return SizedBox();
//                     })),
//                   );
//                 }
//                 return SizedBox();
//               },
//             )
//           ],
//         ),
//         // appBar: AppBar(
//         //   title: const Text('Video player example'),
//         //   actions: <Widget>[
//         //     IconButton(
//         //       key: const ValueKey<String>('push_tab'),
//         //       icon: const Icon(Icons.navigation),
//         //       onPressed: () {
//         //         Navigator.push<_PlayerVideoAndPopPage>(
//         //           context,
//         //           MaterialPageRoute<_PlayerVideoAndPopPage>(
//         //             builder: (BuildContext context) => _PlayerVideoAndPopPage(),
//         //           ),
//         //         );
//         //       },
//         //     )
//         //   ],
//         //   bottom: const TabBar(
//         //     isScrollable: true,
//         //     tabs: <Widget>[
//         //       Tab(
//         //         icon: Icon(Icons.cloud),
//         //         text: "Remote",
//         //       ),
//         //     ],
//         //   ),
//         // ),
//         // body: TabBarView(
//         //   children: <Widget>[
//         //     _BumbleBeeRemoteVideo(),
//         //   ],
//         // ),
//         body: Stack(
//           alignment: Alignment.center,
//           children: [
//             Positioned.fill(
//               child: NativeVideoPlayer(
//                 key: videoKey,
//                 dataSource:
//                     'https://content.uplynk.com/channel/e14e99c0c4e649df909f7b8f9d71c4b0.m3u8?tc=1&exp=1645560109154&rn=302437896&ct=c&oid=b92f1acf21954e588dce50018740e3e8&cid=e14e99c0c4e649df909f7b8f9d71c4b0&ad=publica&ad.site_id=1784&hlsver=6&site_page=https%3A%2F%2Fbutaca.tv%2F&ad.format=vast&ad.slot_count=4&ad.cb=%7B%7BCACHEBUSTER%7D%7D&ad.ip=118.68.63.231&ua=Mozilla%2F5.0%20(Macintosh%3B%20Intel%20Mac%20OS%20X%2010_15_7)%20AppleWebKit%2F605.1.15%20(KHTML%2C%20like%20Gecko)%20Version%2F15.2%20Safari%2F605.1.15&ad.player_height=1920&ad.player_width=1080&ad.content_id=zac000022&ad.content_title=Capulina%20contra%20los%20monstruos&ad.us_privacy=1&ad.content_genre=drama&ad.content_length=5400&ad.livestream=1&ad.content_prodqual=1&sig=6980118b952c2083529486daf77fab4344c6461a54f86d1957e187b7856819fd',
//                 // dataSource: 'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
//                 onCreated: (controller) {
//                   videoController.value = controller;
//                 },
//               ),
//             ),
//             Positioned(
//               bottom: 100,
//               child: ValueListenableBuilder<NativeVideoController?>(
//                 valueListenable: videoController,
//                 builder: (context, controller, _) {
//                   if (controller != null) {
//                     return ChangeNotifierProvider.value(
//                       value: controller,
//                       child: Consumer<NativeVideoController>(builder: ((context, value, child) {
//                         return Text(value.value.caption.text);
//                       })),
//                     );
//                   }
//                   return SizedBox();
//                 },
//               ),
//             ),
//           ],
//         ),
//       ),
//     );
//   }
// }

// class _BumbleBeeRemoteVideo extends StatefulWidget {
//   @override
//   _BumbleBeeRemoteVideoState createState() => _BumbleBeeRemoteVideoState();
// }

// class _BumbleBeeRemoteVideoState extends State<_BumbleBeeRemoteVideo> {
//   late VideoPlayerController _controller;

//   Future<ClosedCaptionFile> _loadCaptions() async {
//     final String fileContents = await DefaultAssetBundle.of(context).loadString('assets/bumble_bee_captions.vtt');
//     return WebVTTCaptionFile(fileContents); // For vtt files, use WebVTTCaptionFile
//   }

//   final subtitleOptions = <SubtitleOption>[];

//   @override
//   void initState() {
//     super.initState();
//     _controller = VideoPlayerController.network(
//       'https://cdn.theoplayer.com/video/elephants-dream/playlist.m3u8',
//       // closedCaptionFile: _loadCaptions(),
//       videoPlayerOptions: VideoPlayerOptions(mixWithOthers: true),
//     );

//     _controller.addListener(() {
//       setState(() {});
//     });
//     _controller.setLooping(true);
//     _controller.initialize().then((value) {
//       _controller.play();
//     });
//     _controller.addListener(onSubtitleOption);
//     _controller.addListener(() {
//       print('subtitle: ${_controller.value.caption.text}');
//     });
//   }

//   void onSubtitleOption() {
//     subtitleOptions.clear();
//     subtitleOptions.addAll(_controller.value.subtitleList);
//     if (subtitleOptions.isNotEmpty) {
//       _controller.removeListener(onSubtitleOption);
//       setState(() {});
//     }
//   }

//   @override
//   void dispose() {
//     _controller.dispose();
//     super.dispose();
//   }

//   @override
//   Widget build(BuildContext context) {
//     return SingleChildScrollView(
//       child: Column(
//         children: <Widget>[
//           // Container(padding: const EdgeInsets.only(top: 20.0)),
//           // const Text('With remote mp4'),
//           Container(
//             padding: const EdgeInsets.all(20),
//             child: AspectRatio(
//               aspectRatio: _controller.value.aspectRatio,
//               child: Stack(
//                 alignment: Alignment.bottomCenter,
//                 children: <Widget>[
//                   VideoPlayer(_controller),
//                   // ClosedCaption(text: _controller.value.caption.text),
//                   _ControlsOverlay(controller: _controller),
//                   VideoProgressIndicator(_controller, allowScrubbing: true),
//                 ],
//               ),
//             ),
//           ),
//           if (subtitleOptions.isNotEmpty)
//             Column(
//               children: subtitleOptions.map((e) {
//                 return TextButton(
//                   onPressed: () {
//                     _controller.setSubtitleOption(e);
//                   },
//                   child: Text(e.label),
//                 );
//               }).toList()
//                 ..add(TextButton(
//                   onPressed: () {
//                     final index = Platform.isIOS ? -1 : 0;
//                     _controller.setSubtitleOption(SubtitleOption({
//                       'trackIndex': index,
//                       'groupIndex': index,
//                     }));
//                   },
//                   child: Text('None'),
//                 )),
//             )
//         ],
//       ),
//     );
//   }
// }

// class _ControlsOverlay extends StatelessWidget {
//   const _ControlsOverlay({Key? key, required this.controller}) : super(key: key);

//   static const _examplePlaybackRates = [
//     0.25,
//     0.5,
//     1.0,
//     1.5,
//     2.0,
//     3.0,
//     5.0,
//     10.0,
//   ];

//   final VideoPlayerController controller;

//   @override
//   Widget build(BuildContext context) {
//     return Stack(
//       children: <Widget>[
//         AnimatedSwitcher(
//           duration: Duration(milliseconds: 50),
//           reverseDuration: Duration(milliseconds: 200),
//           child: controller.value.isPlaying
//               ? SizedBox.shrink()
//               : Container(
//                   color: Colors.black26,
//                   child: Center(
//                     child: Icon(
//                       Icons.play_arrow,
//                       color: Colors.white,
//                       size: 100.0,
//                     ),
//                   ),
//                 ),
//         ),
//         GestureDetector(
//           onTap: () {
//             controller.value.isPlaying ? controller.pause() : controller.play();
//           },
//         ),
//         Align(
//           alignment: Alignment.topRight,
//           child: PopupMenuButton<double>(
//             initialValue: controller.value.playbackSpeed,
//             tooltip: 'Playback speed',
//             onSelected: (speed) {
//               controller.setPlaybackSpeed(speed);
//             },
//             itemBuilder: (context) {
//               return [
//                 for (final speed in _examplePlaybackRates)
//                   PopupMenuItem(
//                     value: speed,
//                     child: Text('${speed}x'),
//                   )
//               ];
//             },
//             child: Padding(
//               padding: const EdgeInsets.symmetric(
//                 // Using less vertical padding as the text is also longer
//                 // horizontally, so it feels like it would need more spacing
//                 // horizontally (matching the aspect ratio of the video).
//                 vertical: 12,
//                 horizontal: 16,
//               ),
//               child: Text('${controller.value.playbackSpeed}x'),
//             ),
//           ),
//         ),
//       ],
//     );
//   }
// }

// class _PlayerVideoAndPopPage extends StatefulWidget {
//   @override
//   _PlayerVideoAndPopPageState createState() => _PlayerVideoAndPopPageState();
// }

// class _PlayerVideoAndPopPageState extends State<_PlayerVideoAndPopPage> {
//   late VideoPlayerController _videoPlayerController;
//   bool startedPlaying = false;

//   @override
//   void initState() {
//     super.initState();

//     _videoPlayerController = VideoPlayerController.asset('assets/Butterfly-209.mp4');
//     _videoPlayerController.addListener(() {
//       if (startedPlaying && !_videoPlayerController.value.isPlaying) {
//         Navigator.pop(context);
//       }
//     });
//   }

//   @override
//   void dispose() {
//     _videoPlayerController.dispose();
//     super.dispose();
//   }

//   Future<bool> started() async {
//     await _videoPlayerController.initialize();
//     await _videoPlayerController.play();
//     startedPlaying = true;
//     return true;
//   }

//   @override
//   Widget build(BuildContext context) {
//     return Material(
//       elevation: 0,
//       child: Center(
//         child: FutureBuilder<bool>(
//           future: started(),
//           builder: (BuildContext context, AsyncSnapshot<bool> snapshot) {
//             if (snapshot.data == true) {
//               return AspectRatio(
//                 aspectRatio: _videoPlayerController.value.aspectRatio,
//                 child: VideoPlayer(_videoPlayerController),
//               );
//             } else {
//               return const Text('waiting for video to load');
//             }
//           },
//         ),
//       ),
//     );
//   }
// }
