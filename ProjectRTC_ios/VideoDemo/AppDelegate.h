//
//  AppDelegate.h
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SocketIOManagerCall.h"
@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (nonatomic, strong) SocketIOManagerCall *socketCall;  /**< <#explain#> */

@end

