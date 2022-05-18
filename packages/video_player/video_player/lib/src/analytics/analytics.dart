// ignore_for_file: public_member_api_docs

import 'package:flutter/services.dart';

class TTAnalytics {
  static const _kMethodChannel = "videoplayer/analytics";
  static TTAnalytics? _sInstance;
  final MethodChannel _channel;
  bool _isInit = false;

  TTAnalytics._() : _channel = MethodChannel(_kMethodChannel);

  factory TTAnalytics.shared() {
    return _sInstance ??= TTAnalytics._();
  }

  Future init({required String key}) async {
    final r = await _channel.invokeMethod('initAnalytics', {'analytics_key': key});
    _isInit = true;
  }

  Future setConfig({required String videoId, String? videoTitle, String? videoUrl, bool? isLive, String? userId}) {
    assert(_isInit = true, 'Must call TTAnalytics.shared().init() first!');
    final m = <String, dynamic>{'video_id': videoId};
    if (videoTitle != null) {
      m['title'] = videoTitle;
    }
    if (videoUrl != null) {
      m['video_url'] = videoUrl;
    }
    if (userId != null) {
      m['user_id'] = userId;
    }
    if (isLive != null) {
      m['is_live'] = isLive;
    }
    return _channel.invokeMethod('setAnalyticsConfig', m);
  }
}
