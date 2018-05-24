//
//  FirstViewController.m
//  VideoDemo
//
//  Created by 风间 on 2018/5/15.
//  Copyright © 2018年 lfj. All rights reserved.
//

#import "FirstViewController.h"
#import "VideoViewController.h"
@interface FirstViewController ()

@end

@implementation FirstViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.view.backgroundColor = [UIColor whiteColor];
    UIButton *bu = [[UIButton alloc] initWithFrame:CGRectMake(70, 100, 100, 80)];
    [bu setBackgroundColor:[UIColor redColor]];
    [bu addTarget:self action:@selector(jump) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:bu];
    
    
}

- (void)jump{
    VideoViewController *we = [[VideoViewController alloc] init];
//    we.callerId = @"1ezKksku79ojjgiGAAAx";
    [self presentViewController:we animated:YES completion:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
