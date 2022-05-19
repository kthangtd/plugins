// ignore_for_file: public_member_api_docs

class TTAnalyticsConfig {
  /// Optional free-form custom data
  final String? customData1;

  /// Optional free-form custom data
  final String? customData2;

  /// Optional free-form custom data
  final String? customData3;

  /// Optional free-form custom data
  final String? customData4;

  /// Optional free-form custom data
  final String? customData5;

  /// Optional free-form custom data
  final String? customData6;

  /// Optional free-form custom data
  final String? customData7;

  /// Optional free-form custom data
  final String? customData8;

  /// Optional free-form custom data
  final String? customData9;

  /// Optional free-form custom data
  final String? customData10;

  /// Optional free-form custom data
  final String? customData11;

  /// Optional free-form custom data
  final String? customData12;

  /// Optional free-form custom data
  final String? customData13;

  /// Optional free-form custom data
  final String? customData14;

  /// Optional free-form custom data
  final String? customData15;

  /// Optional free-form custom data
  final String? customData16;

  /// Optional free-form custom data
  final String? customData17;

  /// Optional free-form custom data
  final String? customData18;

  /// Optional free-form custom data
  final String? customData19;

  /// Optional free-form custom data
  final String? customData20;

  /// Optional free-form custom data
  final String? customData21;

  /// Optional free-form custom data
  final String? customData22;

  /// Optional free-form custom data
  final String? customData23;

  /// Optional free-form custom data
  final String? customData24;

  /// Optional free-form custom data
  final String? customData25;

  /// Optional free-form custom data
  final String? customData26;

  /// Optional free-form custom data
  final String? customData27;

  /// Optional free-form custom data
  final String? customData28;

  /// Optional free-form custom data
  final String? customData29;

  /// Optional free-form custom data
  final String? customData30;

  /// User ID of the customer
  final String? customerUserId;

  /// Experiment name needed for A/B testing
  final String? experimentName;

  /// ID of the video in the CMS system
  final String videoId;

  /// Human readable title of the video asset currently playing
  final String? title;

  /// Breadcrumb path to show where in the app the user is
  final String? path;

  /// Flag to see if stream is live before stream metadata is available (default: false)
  final bool isLive;

  /// Flag to enable Ad tracking
  final bool ads;

  /// How often the video engine should heartbeat
  final int heartbeatInterval;

  /// Flag to use randomised userId not depending on device specific values
  final bool randomizeUserId;

  TTAnalyticsConfig({
    this.customData1,
    this.customData2,
    this.customData3,
    this.customData4,
    this.customData5,
    this.customData6,
    this.customData7,
    this.customData8,
    this.customData9,
    this.customData10,
    this.customData11,
    this.customData12,
    this.customData13,
    this.customData14,
    this.customData15,
    this.customData16,
    this.customData17,
    this.customData18,
    this.customData19,
    this.customData20,
    this.customData21,
    this.customData22,
    this.customData23,
    this.customData24,
    this.customData25,
    this.customData26,
    this.customData27,
    this.customData28,
    this.customData29,
    this.customData30,
    this.customerUserId,
    this.experimentName,
    required this.videoId,
    this.title,
    this.path,
    this.isLive = false,
    this.ads = false,
    this.heartbeatInterval = 59000,
    this.randomizeUserId = false,
  });

  Map asMap() {
    final map = {};
    map['customerUserId'] = customerUserId;
    map['experimentName'] = experimentName;
    map['videoId'] = videoId;
    map['title'] = title;
    map['path'] = path;
    map['isLive'] = isLive;
    map['ads'] = ads;
    map['heartbeatInterval'] = heartbeatInterval;
    map['randomizeUserId'] = randomizeUserId;
    map['customData1'] = customData1;
    map['customData2'] = customData2;
    map['customData3'] = customData3;
    map['customData4'] = customData4;
    map['customData5'] = customData5;
    map['customData6'] = customData6;
    map['customData7'] = customData7;
    map['customData8'] = customData8;
    map['customData9'] = customData9;
    map['customData10'] = customData10;
    map['customData11'] = customData11;
    map['customData12'] = customData12;
    map['customData13'] = customData13;
    map['customData14'] = customData14;
    map['customData15'] = customData15;
    map['customData16'] = customData16;
    map['customData17'] = customData17;
    map['customData18'] = customData18;
    map['customData19'] = customData19;
    map['customData20'] = customData20;
    map['customData21'] = customData21;
    map['customData22'] = customData22;
    map['customData23'] = customData23;
    map['customData24'] = customData24;
    map['customData25'] = customData25;
    map['customData26'] = customData26;
    map['customData27'] = customData27;
    map['customData28'] = customData28;
    map['customData29'] = customData29;
    map['customData30'] = customData30;
    return map;
  }
}
