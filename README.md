基础工具包
=====
> 请使用最新的稳定版，所有 SNAPSHOT 版本不保证可用性。

### 各子项目概述
1. iprd-common-base 各子模块的公用基础 jar 包。
2. iprd-common-aop 基于 spring-mvc 的切片组件。
3. iprd-common-message 通用的内存队列，可以方便的限制流量。
4. iprd-common-view 基于 spring-mvc，封装一些视图类的使用，比如 Excel 导出视图。

### 发布步骤
* 发布 release 版本步骤如下，此时会自动迭代版本号。

```sh
$ cd ~/workspce/iprd-commons
$ mvn release:prepare
$ mvn release:preform
```

* 发布 snapshot 版本步骤如下，不会迭代版本号。

```sh
$ cd ~/workspace/iprd-commons
$ mvn deploy
```