//
//  PlayerWrapper.swift
//  video_player
//
//  Created by Ngô Thắng Lợi on 19/05/2022.
//

import Foundation
import BitmovinAnalyticsCollector

@objc
@objcMembers
public class AVPlayerCollectorWrapper: NSObject {
    private var collector: AVPlayerCollector!
    
    public init(config: BitmovinAnalyticsConfig) {
        super.init()
        collector = AVPlayerCollector(config: config)
    }
    
    @objc
    public func attachPlayer(player: AVPlayer) {
        collector.attachPlayer(player: player)
    }

    @objc
    public func detachPlayer() {
        collector.detachPlayer()
    }
    
}
