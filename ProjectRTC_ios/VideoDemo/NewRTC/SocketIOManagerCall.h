//
//  SocketIOManagerCall.h
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import <Foundation/Foundation.h>
@import SocketIO;
static NSString * const socketIOURL = @"https://192.168.109.144:18081";

@interface SocketIOManagerCall : NSObject<NSURLSessionDelegate>
@property (nonatomic, retain) SocketIOClient *socket;
@property (nonatomic, strong) SocketManager *manager;

+ (instancetype)sharedManager;

- (void)connect;
- (void)disconnect;

- (void)emitMessage:(NSDictionary *)messageDict;
- (void)emitReadyToStream:(NSDictionary *)messageDict;
@end
