// ignore_for_file: public_member_api_docs

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'native_video_controller.dart';

class NativeVideoPlayer extends StatefulWidget {
  final String dataSource;
  final Map<String, String> httpHeaders;

  final ValueChanged<NativeVideoController>? onCreated;

  const NativeVideoPlayer({
    Key? key,
    required this.dataSource,
    this.httpHeaders = const {},
    this.onCreated,
  }) : super(key: key);

  @override
  _NativeVideoPlayerState createState() => _NativeVideoPlayerState();
}

class _NativeVideoPlayerState extends State<NativeVideoPlayer> {
  @override
  Widget build(BuildContext context) {
    const String viewType = 'videoNativePlayer';
    // Pass parameters to the platform side.
    final Map<String, dynamic> creationParams = <String, dynamic>{
      'dataSource': widget.dataSource,
      'httpHeaders': widget.httpHeaders,
    };

    return AndroidView(
      viewType: viewType,
      layoutDirection: TextDirection.ltr,
      creationParams: creationParams,
      creationParamsCodec: const StandardMessageCodec(),
      onPlatformViewCreated: (id) {
        final controller = NativeVideoController(
          widget.dataSource,
          httpHeaders: widget.httpHeaders,
          viewId: id,
        );
        widget.onCreated?.call(controller);
      },
    );
  }
}
