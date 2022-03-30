// ignore_for_file: public_member_api_docs

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'ads_video_controller.dart';

class AdsVideoPlayer extends StatefulWidget {
  final AdsVideoController controller;

  const AdsVideoPlayer({Key? key, required this.controller}) : super(key: key);

  @override
  _AdsVideoPlayerState createState() => _AdsVideoPlayerState();
}

class _AdsVideoPlayerState extends State<AdsVideoPlayer> with AutomaticKeepAliveClientMixin<AdsVideoPlayer> {
  final Map<String, dynamic> creationParams = {};

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    creationParams.addAll({
      'dataSource': widget.controller.dataSource,
      'httpHeaders': widget.controller.httpHeaders,
      'vast_tag_url': widget.controller.vastTagUrl,
      'ad_breaks': widget.controller.adBreaks,
    });
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return AndroidView(
      viewType: 'AdsVideoPlayer',
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: (viewId) {
        widget.controller.init(viewId);
      },
    );
  }
}
