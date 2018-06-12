# WebRtc
##一整套WebRtc Demo 包含服务端、移动端以及Web
服务端：node.js 根据该https://github.com/pchab/ProjectRTC 进行相应修改，增加Https服务，修复若干bug

## Install
It requires [node.js](http://nodejs.org/download/)
* npm install
* npm start

You can test it in the (Chrome or Firefox) browser at localhost:18081 or localhost:18080. 
18081是https服务(尽量用https服务 因为谷歌浏览器 想打开一些硬件 必须是Https)
18080 是http服务

Android：https://github.com/pchab 这里配套的android WebRtc项目过老，socket连接的服务都是http
，同时在华为P8及以上手机都会出现适配问题（相机打不开问题），还有若干关闭摄像头，关闭多媒体流崩溃的问题，
故根据node服务相应信令（例加入房间，退出房间等指令） 用最新的Libjingle进行更改android项目

IOS:同样的问题，链接的socket服务都只有http配置

——————————————————————————————————————————

# WebRtc笔记：

### 什么是WebRTC？先来了解下
https://www.openwebrtc.cn/?p=1

### webRtc解决方案 
https://blog.csdn.net/gupar/article/details/53101435

### webrtc进阶-信令篇-之三：信令、stun、turn、ice
https://blog.csdn.net/fireroll/article/details/50780863

### p2p技术有什么优缺点（webRtc 底层的多媒体流传输协议 还是p2p协议）
http://www.elecfans.com/news/wangluo/20171216604281.html

### P2P的原理和常见的实现方式
https://www.cnblogs.com/programmer-wfq/p/6709182.html

### google Turn服务器
https://github.com/coturn/coturn
https://blog.csdn.net/qq_16042523/article/details/52994785

### webrtc学习: 部署stun和turn服务器
https://www.cnblogs.com/lingdhox/p/4209659.html

### coturn(turn)服务器搭建(编译源码) 有图
https://blog.csdn.net/qq_16042523/article/details/52994785

### Libjingle介绍（就是P2P传输的开源库，由google公司开发）
https://blog.csdn.net/byxdaz/article/details/52786476

### 三个基于WebRTC开源MCU框架的横向对比
https://blog.csdn.net/xiaoluer/article/details/79088416

——————————————————————————————————————

# 在NAT穿墙遇到的问题
1、因为公司所在项目 外部设备（移动端）是通过VPN连接到内部服务器（整个项目是处于局域网内），所以只需要运维对VPN配置一下，
在传输多媒体流阶段就不会出现问题

### 常规VPN不能越过路由器NAT的原因（有些VPN不能实现NAT穿墙）
https://bbs.csdn.net/topics/360130768/
