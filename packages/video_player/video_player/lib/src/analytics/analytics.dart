// ignore_for_file: public_member_api_docs, unused_field

import 'package:flutter/services.dart';
import 'analytics_config.dart';

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
    await _channel.invokeMethod('initAnalytics', {'analytics_key': key});
    _isInit = true;
  }

  Future setConfig(TTAnalyticsConfig config) {
    assert(_isInit = true, 'Must call TTAnalytics.shared().init() first!');
    return _channel.invokeMethod('setAnalyticsConfig', config.asMap());
  }
}
