# NeoIconPack 图标包APP模板

NeoIconPack 是一款现代化的图标包应用，采用 Kotlin 和 Jetpack Compose 技术栈重构而成。这是一个轻量而强大的图标包模板，支持主流启动器，提供图标申请及统计功能，并集成了多种实用工具。

基于此模板进行二次开发非常简便，您只需装配图标、修改少量配置文件，无需深入了解复杂的代码实现，即可打包出独具特色的个人图标包作品。

相比前身 NanoIconPack，该项目拥有更现代的界面设计。


### 下文目录

+ [支持启动器](#支持启动器)
+ [实用功能](#实用功能)
+ [服务器支持](#服务器支持)

其他：
+ [联系开发者](#联系开发者)
+ [License](#license)


### 支持启动器

目前核心支持以下3个元老级的启动器：

NanoIconPack 同时还支持许多未列出的启动器，比如
+ Smart Launcher Pro
+ Action 3
+ Aviate
+ Holo Launcher
+ Arrow桌面
+ S桌面
+ Hola桌面
+ Go桌面
+ 冷桌面
+ 等……

以及一些系统默认启动器，比如
+ Xperia Home Launcher
+ 氢桌面
+ 等……

> 已知不支持并不打算支持：
> + ~~TSF桌面~~
> + ~~Atom桌面~~


### 实用功能

除了图标包最基本的功能外，我们还为 NanoIconPack 开发了一些有用的辅助功能。以下列举一二，更多请下载 Sample APP 体验。

+ 主界面三大页
  + 「已适配」：从全部图标中筛选出其对应APP已安装的部分列出
  + 「全部」：展示图标包内全部的图标
  + 「未适配」：从已安装的APP中筛选出无图标适配的部分列出
    + 支持一键提交适配申请
    + 支持复制APP代码（长按菜单中）
    + 支持保存APP图标（长按菜单中）

+ 图标查看
  + 栅格线叠加以了解边距等信息
  + 同时展示已安装APP的图标进行对比
  + 可替换的图标（为同一APP准备了多个图标，非默认的图标）进行**` ALT `**标记
  + 保存图标
  + 已适配的图标可一键发送到桌面（快捷方式形式）
  
+ 图标搜索
  
  支持按图标名、APP名或包名进行模糊搜索

+ 「更新了啥」页面展示每一版更新的图标
  
+ 图标申请适配统计
  + 可进行已适配/未适配标记
  + 可跳转应用商店查看
  + 可复制APP代码
  
  > 该界面默认不可见，进入方法：「关于」界面进入或双击主界面底栏的 **未适配** 图标

+ 版权描述
  + 作者
  + 联系方式
  + 捐赠渠道（支付宝直跳、微信二维码或其他）
  + 「申请统计」入口
  + 网页工具入口
  
+ 支持启动器的手动替换图标


### 服务器支持

NanoIconPack 拥有一个轻量的服务器，提供图标申请和申请统计两大服务。

详情请移步 [NanoIconPack 服务端项目](https://github.com/by-syk/NanoIconPackServer)了解。

### 联系开发者

+ E-mail: [By_syk@163.com](mailto:By_syk@163.com "By_syk")
+ 酷安主页：[@By_syk](https://www.coolapk.com/u/463675)


### License

    Copyright 2017-2018 By_syk

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


*Copyright &#169; 2017-2018 By_syk. All rights reserved.*