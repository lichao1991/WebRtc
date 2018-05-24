//
//  VideoViewController.m
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import "VideoViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <Toast/UIView+Toast.h>

#import "AppDelegate.h"
@interface VideoViewController ()

@property (nonatomic, strong) UIButton *hangupButton;  /**< 挂断 */

@end

@implementation VideoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];

//    [[SocketIOManagerCall sharedManager] connect];
    
    AppDelegate *appdelegat = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    appdelegat.socketCall = [[SocketIOManagerCall alloc] init];
    [appdelegat.socketCall connect];
    [RTCPeerConnectionFactory initializeSSL];
//
    [self.view addSubview:self.remoteView];
    [self.view addSubview:self.localView];
    [self.view addSubview:self.hangupButton];
    
    [self initz];

}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];

    [[NSNotificationCenter defaultCenter] removeObserver:self];

    [self disconnect];
}
- (void)disconnect {
    if (self.webRTCClient) {
        if (self.localVideoTrack){
            [self.localVideoTrack removeRenderer:self.localView];
        }
        if (self.remoteVideoTrack){
            [self.remoteVideoTrack removeRenderer:self.remoteView];
        }
        self.localVideoTrack = nil;
        self.remoteVideoTrack = nil;
        [self.webRTCClient disconnect];
    }
//    [[SocketIOManagerCall sharedManager] disconnect];
    AppDelegate *appdelegat = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    [appdelegat.socketCall.socket disconnect];
    [appdelegat.socketCall.manager disconnect];
    appdelegat.socketCall.socket = nil;
    appdelegat.socketCall.manager = nil;
    [self dismissViewControllerAnimated:YES completion:NULL];

}


- (void)remoteDisconnected {
    if (self.remoteVideoTrack) {
        [self.remoteVideoTrack removeRenderer:self.remoteView];
    }
    
    self.remoteVideoTrack = nil;

}


- (void)initz{
    
    NSInteger videoWidth = [UIScreen mainScreen].bounds.size.width;
    NSInteger videoHeight = [UIScreen mainScreen].bounds.size.height;
    
    PeerConnectionParameters *params = [[PeerConnectionParameters alloc] initWithParams:YES loopback:NO videoWidth:videoWidth videoHeight:videoHeight videoFps:30 videoStartBitrate:1 videoCodec:VIDEO_CODEC_VP9 videoCodecHwAcceleration:YES audioStartBitrate:1 audioCodec:AUDIO_CODEC_OPUS cpuOveruseDetection:YES];
    self.webRTCClient = [[WebRTCClient alloc] initWebRTCClient:self params:params];
}


- (RTCEAGLVideoView *)remoteView{
    if (!_remoteView) {
        _remoteView = [[RTCEAGLVideoView alloc] initWithFrame:CGRectMake(80, 50, 150, 200)];
        _remoteView.backgroundColor = [UIColor yellowColor];
        _remoteView.delegate = self;
    }
    return _remoteView;
}
- (RTCEAGLVideoView *)localView{
    if (!_localView) {
        _localView = [[RTCEAGLVideoView alloc] initWithFrame:CGRectMake(80, 280, 150, 200)];
        _localView.backgroundColor = [UIColor blueColor];
        _localView.delegate = self;
    }
    return _localView;
}
- (UIButton *)hangupButton{
    if (!_hangupButton) {
        _hangupButton = [[UIButton alloc] initWithFrame:CGRectMake(250, 100, 100, 80)];
        [_hangupButton setBackgroundColor:[UIColor redColor]];
        [_hangupButton addTarget:self action:@selector(dismissViewVC) forControlEvents:UIControlEventTouchDown];
    }
    return _hangupButton;
}
- (void)dismissViewVC{
    [self disconnect];
}

- (void)call:(NSString *)callId {
    [self startCam];
    NSString *message = [NSString stringWithFormat:@"%@/%@", socketIOURL, callId];
    NSLog(@"用这个连接打我: %@", message);
}
- (void)startCam {
    NSString *name = [NSString stringWithFormat:@"ios_%@", [self getStringFromDateTime]];
    NSLog(@" startCam name: %@", name);
    //camera settings
    [self.webRTCClient start:name];
}
- (NSString *)getStringFromDateTime{
    NSDateFormatter *dateFormatter=[[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    return [dateFormatter stringFromDate:[NSDate date]];
}

- (void)answer:(NSString *)callerId {
    NSLog(@"接听的id answer callerId:%@", callerId);
    [self.webRTCClient sendMessage:callerId type:KEY_INIT payload:nil];
    [self startCam];
}


#pragma mark - implement WebRTCClient Delegate
- (void)onCallReady:(NSString *)callId {
    NSLog(@"准备通话 onCallReady callId: %@", callId);
    if(self.callerId.length > 0) {
        [self answer:self.callerId];
    }else {
        [self call:callId];
        self.myCallId = callId;
    }
}
- (void)onStatusChanged:(WebRTCClientState)newStatus {
    
    if(newStatus == kWebRTCClientStateConnecting){
        NSLog(@"连接中 WebRTCClientState: Connecting");
        [self.view makeToast:@"Connecting"];
    }else if(newStatus == kWebRTCClientStateConnected){
        NSLog(@"已经连接 WebRTCClientState: Connected");
        [self.view makeToast:@"Connected"];
    }else if(newStatus == kWebRTCClientStateDisconnected){
        NSLog(@"断开连接 WebRTCClientState: Disconnected");
        [self.view makeToast:@"Disconnected"];
//        [self remoteDisconnected];
        [self disconnect];
    }
}

- (void)onLocalStream:(RTCMediaStream *)localStream {
    
    NSLog(@"获取本地视频流onLocalStream");
    
    RTCVideoTrack *localVideoTrack = localStream.videoTracks[0];
    if (self.localVideoTrack) { //clear old data
        [self.localVideoTrack removeRenderer:self.localView];
        self.localVideoTrack = nil;
    }
    self.localVideoTrack = localVideoTrack;
    [self.localVideoTrack addRenderer:self.localView];
}


//didReceiveRemoteVideoTrack
- (void)onAddRemoteStream:(RTCMediaStream *) remoteStream endPoint:(NSInteger)endPoint {
    
    NSLog(@"获取对方视频流onAddRemoteStream");
    
    if (self.remoteVideoTrack) { //clear old data
        [self.remoteVideoTrack removeRenderer:self.remoteView];
        self.remoteVideoTrack = nil;
    }
    self.remoteVideoTrack = remoteStream.videoTracks[0];
    [self.remoteVideoTrack addRenderer:self.remoteView];
}

- (void)onRemoveRemoteStream:(NSInteger) endPoint {
    NSLog(@"删除对方视频流");
    //    VideoRendererGui.update(localRender,
    //                            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
    //                            LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
    //                            scalingType);
}


- (void)didError:(NSError *) error{
    
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Error" message:error.localizedDescription preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault handler:nil];
    [alert addAction:okAction];
    [self presentViewController:alert animated:YES completion:nil];
    
}



#pragma mark - RTCEAGLVideoViewDelegate
- (void)videoView:(RTCEAGLVideoView *)videoView didChangeVideoSize:(CGSize)size {
    
}



@end
