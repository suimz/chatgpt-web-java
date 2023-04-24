# ChatGPT-Web-Java

简体中文 | [English](https://github.com/suimz/ChatGPT-Web-Java/blob/master/README_EN.md)

使用 `Java` + `Spring` 技术栈适配 [Chanzhaoyu/chatgpt-web][ChatGPT-Web] 前端项目接口实现的 `Java` 版服务器端，开源用于个人参考学习。

------------------------------

![chat](./screenshot/chat.png)

## 快速使用

```
docker run \
  -it -d \
  --name chatgpt-web-java \
  -p 8080:8080 \
  hubsuimz/chatgpt-web-java:0.0.1-full \
  --app.openai-api-key=你的ApiKey
```

访问: http://你的域名orIP:8080

> `版本号-full` TAG 的镜像整合了 [ChatGPT-Web] 的前端页面，可直接访问使用。

## 介绍

以下描述内容摘自于 [ChatGPT-Web] ，本项目基于该项目API接口的交互流程进行实现，感谢 [@Chanzhaoyu](https://github.com/Chanzhaoyu) 提供的优质项目。

支持双模型，提供了两种非官方 `ChatGPT API` 方法

| 方式                                          | 免费？ | 可靠性   | 量    |
|---------------------------------------------|-----|-------|------|
| `ChatGPTAPI(gpt-3.5-turbo-0301)`            | 否   | 可靠    | 相对较笨 |
| `ChatGPTUnofficialProxyAPI(网页 accessToken)` | 是   | 相对不可靠 | 聪明   |

对比：
1. `ChatGPTAPI` 使用 `gpt-3.5-turbo` 通过 `OpenAI` 官方 `API` 调用 `ChatGPT`。
2. `ChatGPTUnofficialProxyAPI` 使用非官方代理服务器访问 `ChatGPT` 的后端`API`，绕过`Cloudflare`（依赖于第三方服务器，并且有速率限制）。

警告：
1. 你应该首先使用 `API` 方式。
2. 使用 `API` 时，如果网络不通，那是国内被墙了，你需要自建代理，绝对不要使用别人的公开代理，那是危险的。
3. 使用 `accessToken` 方式时反向代理将向第三方暴露您的访问令牌，这样做应该不会产生任何不良影响，但在使用这种方法之前请考虑风险。
4. 使用 `accessToken` 时，不管你是国内还是国外的机器，都会使用代理。默认代理为 [@acheong08](https://github.com/acheong08) 大佬的 `https://bypass.churchless.tech/api/conversation`，这不是后门也不是监听，除非你有能力自己翻过 `CF` 验证，用前请知悉。
5. 把项目发布到公共网络时，你应该设置 `app.auth-secret-key` 参数添加你的密码访问权限。

## 版本说明

这是开发服务器端时，为了适配 [ChatGPT-Web] 的版本对照表，不保证当前项目能够兼容高于下面记录的 [ChatGPT-Web] 版本。

| ChatGPT-Web-Java | ChatGPT-Web |
|------------------|-------------|
| 0.0.1            | 2.10.9      |

## 项目技术栈

- JDK 11+（8没试过）  
- Gradle 7+  
- Spring-Boot 2.7.10  
- OpenAI-Java 0.12.0 项目地址：[https://github.com/TheoKanning/openai-java](https://github.com/TheoKanning/openai-java)  

## 部署独立后端

### 使用 Docker

#### 方式一

```
docker run \
  -it -d \
  --name chatgpt-web-java \
  -p 8081:8080 \
  hubsuimz/chatgpt-web-java \
  --app.openai-api-key=你的ApiKey
```

#### 方式二

```
docker run \
  -it -d \
  --name chatgpt-web-java \
  -p 8080:8080 \
  -v ~/chatgpt-web-java:/app/config \
  hubsuimz/chatgpt-web-java
```

方式二首次运行会启动失败，你需要修改上面映射出来的配置文件 `~/chatgpt-web-java/application-app.properties` 中的 `app.openai-api-key` 等参数，然后重启 container：

```
docker restart chatgpt-web-java
```

### 手动打包

这是一个标准的 `Spring Boot` 工程，想必聪明的你一定非常熟悉这个框架了!

你可以使用自己熟悉的 IDEA 进行 build，也可以在源代码根路径执行编译脚本：

```
./gradlew bootJar
```

编译出来的 `Jar` 所在位置：`项目根路径/build/libs/app.jar`

接下来怎么运行我就用不说了吧， 我才不会告诉你使用 `java -jar` 命令。

**其他：**

如果你希望将 [ChatGPT-Web] 打包到项目中，可进行如下操作：

1. 打包 [ChatGPT-Web] 前端项目，如何打包请查阅：[这里](https://github.com/Chanzhaoyu/chatgpt-web#%E5%89%8D%E7%AB%AF%E7%BD%91%E9%A1%B5-1)  。
2. 将 `dist` 目录中的所有文件拷贝到 `src/main/resources/static` 目录下。

## 参数列表

| 参数名                                | 必填                                  | 备注                                                                                              |
|------------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------|
| `app.auth-secret-key`              | 可选                                  | [ChatGPT-Web] 访问密钥，当你部署到公网时，建议配置                                                                |
| `app.max-request-per-hour`         | 可选                                  | 每个IP每小时最大聊天请求次数，可选，默认无限                                                                         |
| `app.api-timeout-ms`               | 可选， 默认：120000                       | API请求超时时间，单位毫秒                                                                                  |
| `app.openai-api-key`               | 和 `access-token` 二选一                | 使用 `OpenAI API` 所需的 `apiKey` [(获取 apiKey)](https://platform.openai.com/overview)                |
| `app.openai-access-token`          | 和 `api-key` 二选一                     | 使用 `Web API` 所需的 `accessToken` [(获取 accessToken)](https://chat.openai.com/api/auth/session)     |
| `app.openai-sensitive-id`          | 可选                                  | 用于查询账号余额 [(获取 sensitiveId)](https://platform.openai.com/account/usage) ，从控制台中的`login` 接口请求结果中获取 |
| `app.openai-api-base-url`          | 可选，`api-key` 时可用                    | `API` 接口地址                                                                                      |
| `app.openai-api-mode`              | 可选，`api-key` 时可用，默认：`gpt-3.5-turbo` | `API` 模型                                                                                        |
| `app.openai-reverse-api-proxy-url` | 可选，`access-token` 时可用               | `Web API` 反向代理地址 [详情](https://github.com/transitive-bullshit/chatgpt-api#reverse-proxy)         |
| `app.socks-proxy.host`             | 可选，`http-proxy` 二选一                 | Socks 代理地址                                                                                      |
| `app.socks-proxy.port`             | 可选                                  | Socks 代理端口                                                                                      |
| `app.socks-proxy.username`         | 可选                                  | Socks 代理账号                                                                                      |
| `app.socks-proxy.password`         | 可选                                  | Socks 代理密码                                                                                      |
| `app.http-proxy.host`              | 可选，`socks-proxy` 二选一                | HTTP 代理地址                                                                                       |
| `app.http-proxy.port`              | 可选                                  | HTTP 代理端口                                                                                       |

详细可查看：`src/main/resources/application-app.properties`

## License

[Apache License 2.0](https://github.com/suimz/ChatGPT-Web-Java/blob/master/LICENSE)

[ChatGPT-Web]: https://github.com/Chanzhaoyu/chatgpt-web