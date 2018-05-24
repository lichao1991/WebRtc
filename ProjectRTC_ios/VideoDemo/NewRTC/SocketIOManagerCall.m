//
//  SocketIOManagerCall.m
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import "SocketIOManagerCall.h"

@implementation SocketIOManagerCall

//+ (instancetype)sharedManager {
//    static SocketIOManagerCall *sharedSocketIOManager = nil;
//    static dispatch_once_t onceToken;
//    dispatch_once(&onceToken, ^{
//        sharedSocketIOManager = [[SocketIOManagerCall alloc] init];
//    });
//    return sharedSocketIOManager;
//}

- (instancetype)init {
    
    if (self = [super init]) {
        
        SSLSecurity *security = [[SSLSecurity alloc] initWithUsePublicKeys:YES];
        self.manager = [[SocketManager alloc] initWithSocketURL:[NSURL URLWithString:socketIOURL] config:@{@"log": @YES , @"forcePolling": @YES,@"security" : security, @"secure" : @YES, @"sessionDelegate":self}];
        self.socket = _manager.defaultSocket;
        
        
    }
    
    return self;
}


- (void)connect {
    
    [self onConnect];
    [self onDisconnect];
    
    [self onId];
    [self onMessage];
    
    [self.socket connect];
}
- (void)disconnect{
    [self.socket disconnect];
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

- (void)emitMessage:(NSDictionary *)messageDict {
    
    if(!messageDict){
        return;
    }
    [self.socket emit:@"message" with:@[messageDict]];
}

- (void)emitReadyToStream:(NSDictionary *)messageDict {
    
    if(!messageDict){
        return;
    }
    [self.socket emit:@"readyToStream" with:@[messageDict]];
    
}


- (void)dealloc {
    // Should never be called, but just here for clarity really.
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
