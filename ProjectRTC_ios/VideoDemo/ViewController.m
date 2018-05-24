//
//  ViewController.m
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import "ViewController.h"
@import SocketIO;
@interface ViewController ()<NSURLSessionDelegate>

@property (nonatomic, strong) SocketIOClient *socket;
@property (nonatomic, strong) SocketManager *manager;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor yellowColor];
    
    SSLSecurity *security = [[SSLSecurity alloc] initWithUsePublicKeys:YES];

    NSURL* url = [[NSURL alloc] initWithString:@"https://192.168.109.144:18081"];
    _manager = [[SocketManager alloc] initWithSocketURL:url config:@{@"log": @YES ,@"security" : security, @"secure" : @YES, @"sessionDelegate":self}];
    _socket = _manager.defaultSocket;

    [self connect];
    
}


- (void)connect {
    
    [self onConnect];
//
    [self onId];
    [self onMessage];
    
    [self.socket connect];
    
    [self performSelector:@selector(duankai) withObject:nil afterDelay:5];
}

- (void)duankai{
//    [self onDisconnect];
    [self.socket disconnect];
    [self.manager disconnect];
    self.manager = nil;
}

- (void)onConnect {
    [self.socket on:@"connect" callback:^(NSArray* data, SocketAckEmitter* ack) {
        NSLog(@"socketiocall connected");
    }];
}

- (void)onDisconnect {
    [self.socket on:@"disconnect" callback:^(NSArray * data, SocketAckEmitter * ack) {
        NSLog(@"socketiocall disconnect");
    }];
}

- (void)onMessage {
    
    [self.socket on:@"message" callback:^(NSArray * arrayResponse, SocketAckEmitter * ack) {
        
        if(arrayResponse.count == 0){
            return;
        }
        
        NSDictionary *json = arrayResponse[0];
        NSLog(@"WSS->C: %@", [json description]);
        
        [[NSNotificationCenter defaultCenter] postNotificationName:@"onMessage" object:json];
        
    }];
    
}

- (void)onId {
    [self.socket on:@"id" callback:^(NSArray * arrayResponse, SocketAckEmitter * ack) {
        
        if(arrayResponse.count == 0){
            return;
        }
        
        NSDictionary *json = arrayResponse[0];
        NSLog(@"Receive socketio -> id: %@", [json description]);
        
        [[NSNotificationCenter defaultCenter] postNotificationName:@"onId" object:json];
        
    }];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)URLSession:(NSURLSession *)session
didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge
 completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler
{
    //AFNetworking中的处理方式
    NSURLSessionAuthChallengeDisposition disposition = NSURLSessionAuthChallengePerformDefaultHandling;
    __block NSURLCredential *credential = nil;
    //判断服务器返回的证书是否是服务器信任的
    if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust]) {
        credential = [NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust];
        /*disposition：如何处理证书
         NSURLSessionAuthChallengePerformDefaultHandling:默认方式处理
         NSURLSessionAuthChallengeUseCredential：使用指定的证书    NSURLSessionAuthChallengeCancelAuthenticationChallenge：取消请求
         */
        if (credential) {
            disposition = NSURLSessionAuthChallengeUseCredential;
        } else {
            disposition = NSURLSessionAuthChallengePerformDefaultHandling;
        }
    } else {
        disposition = NSURLSessionAuthChallengeCancelAuthenticationChallenge;
    }
    //安装证书
    if (completionHandler) {
        completionHandler(disposition, credential);
    }
}

@end
