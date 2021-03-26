## 序

    该工程的初衷在于简化新工程的框架搭建、提高新工程开发效率，使开发者把时间集中在业务开发上。设计原则：各模块高度集成、职责单一
    工程集成了okhttp+retrofit、glide、eventBus、objectBox等主流库，且做了简易封装，也加入了toast之类的工具类，方便调用。
    
    使用姿势：
    1、普通activity、fragment继承baseActivity、baseFragmen使用即可，基类里提供了常用的方法，如网络请求、权限。
    2、网页页面可继承CommWebActivity
    3、baseActivity提供了网络状态处理及对应状态页面的展示，请灵活运用
    4、公用类的资源尽量放到commo中，其他则可以放到对应的业务模块
    5、打包配置统一在module_app的gradle和工程的gradle配置

## 工程结构
    
                  module_app  <- - - - app_360/app_yyb...
                       |
          +------------+-----------+
          |            |           |
     module_main    module1     module2...
          |            |           |
          +------------+-----------+
                       |
                  lib_common

    注：整个结构从下往上依赖
    
    1、module功能说明：
        a.module_app
            壳app，负责多渠道icon、appName配置、打包编译、混淆配置
            依赖不同子module可编译成不同的APP
    
        b.module_main
            APP的主module，启动页面、登录、主页等页面在该module
    
        c.module1...
            功能组件模块，按需求划分为单模块开发
    
        d.lib_common
            整个工程通用部分在该module中，如第三方sdk、工具类、自定义View、网络、数据库、BaseActivity、BaseApp...
            子组件按需求依赖该module
    
        e.app_360/app_yyb...
            可选扩展，依赖在module_app中，这些module的存在是为了不同渠道使用不同启动页和图标（在module_app中通过gradle配置）

    2、注意事项：
        a.单module运行
            1.只需在工程级gradle中的isModule打开选择对应module运行即可
    
        b.不同module间页面跳转
            1.不同module之间页面跳转采用ARouter框架进行页面路由，具体使用方法请查阅官方文档[https://github.com/alibaba/ARouter]
    
        c.不同module间通信
            采用EventBus（也可以用Broadcast）
    
        d.混淆
            1.整个工程的混淆在module_app中配置即可，其他module无需单独配置（如果单module运行有需要的话可以单另配置）

## 代码规范
    1.layout文件名:模块简称_页面/视图名，如：xx_activity_main
    2.图片命名：模块简称_ic_业务功能，如：xx_ic_main_arrow
    


