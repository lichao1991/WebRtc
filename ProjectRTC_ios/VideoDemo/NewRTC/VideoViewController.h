//
//  VideoViewController.h
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "WebRTCClient.h"

#import <libjingle_peerconnection/RTCVideoRenderer.h>
#import <libjingle_peerconnection/RTCOpenGLVideoRenderer.h>


static NSString * const VIDEO_CODEC_VP9 = @"VP9";
static NSString * const AUDIO_CODEC_OPUS = @"opus";

@interface VideoViewController : UIViewController<WebRTCClientDelegate, RTCEAGLVideoViewDelegate>
@property (nonatomic, strong) WebRTCClient *webRTCClient;

@property (nonatomic, strong) RTCEAGLVideoView *remoteView;  /**< 对方视频 */
@property (nonatomic, strong) RTCEAGLVideoView *localView;  /**< 本地视频 */

@property (strong, nonatomic) RTCVideoTrack *localVideoTrack;
@property (strong, nonatomic) RTCVideoTrack *remoteVideoTrack;

@property (nonatomic) NSString *callerId;
@property (nonatomic) NSString *myCallId;

@end
