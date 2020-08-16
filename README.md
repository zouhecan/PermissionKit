# PermissionKit
A easy permission kit for Android.

Android app开发始终绕不开申请权限，而申请权限的代码与业务代码耦合在一起早已让开发者们深恶痛绝，于是就诞生了一些方便开发者操作的权限框架，并且不断有新的优化被提出用于解决框架的不足。
然而时至今日，还是很难看到一款真正完全业务解耦，并能够处理重复和连续权限请求的框架。
本文详细分析了现有Android权限请求方式存在的痛点，并在此基础上，封装了一个便捷实用的权限请求框架，尤其在处理连续请求的设计上做足了文章。

使用本框架能为你的Android项目带来的好处在于：

1、申请权限具体操作放到PermissionFragment中进行，使业务方能直接在当前页完成权限操作，数据传输链路直观清晰、无污染。

2、PermissionUtil作为app所有权限操作的统一收口，使得业务方在在需要权限操作时变得方便简单，项目代码变得规范易维护。

3、对“禁止后不再提示”的权限，再次发起请求时，为了不出现无响应的用户体验，展示引导弹窗让用户有感知，并引导用户去设置页中开启权限，并在内部统一完成设置页用户操作监听，完成请求，避免无响应。

4、对相同权限的重复请求，进行合并，在一次请求结果回来后，一一回调各个请求，避免重复请求的同时还能一一给予回调。

5、对不同权限的连续请求，不简单丢弃后发起的请求，展示自定义的等待请求弹窗，让用户在前一个请求结束后，还能再处理后一个请求，不丢失任何请求，保证用户体验。
