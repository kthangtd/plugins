// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import <Flutter/Flutter.h>
#import <AVFoundation/AVFoundation.h>
#import <GLKit/GLKit.h>

@interface FLTVideoPlayerPlugin : NSObject <FlutterPlugin>
@end

@interface TTAnalytics : NSObject

+ (instancetype)shared;

- (void)init:(id<FlutterBinaryMessenger>)binaryMessenger;

- (void)detachPlayerWithId:(NSString*)id;

- (void)attachPlayer:(AVPlayer*)player withId:(NSString*)id;

@end
