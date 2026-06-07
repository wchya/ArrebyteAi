# ArrebyteAi

ArrebyteAi 是一个基于 Spring Boot 和 Spring AI 的大模型聊天服务示例项目。项目当前提供 OpenAI 与 Anthropic 两类 `ChatClient`，并通过 REST 接口暴露基础聊天能力。

## 技术栈

- Java 17
- Spring Boot 3.5.14
- Spring AI 1.0.8
- Maven Wrapper
- OpenAI Chat Model Starter
- Anthropic Chat Model Starter
- Lombok

## 项目结构

```text
.
├── pom.xml
├── mvnw
├── src
│   ├── main
│   │   ├── java/cn/arrebyte/ai/arrebyteai
│   │   │   ├── ArrebyteAiApplication.java
│   │   │   ├── configuration/ChatConfiguration.java
│   │   │   └── controller/AiChatController.java
│   │   └── resources/application.properties
│   └── test
│       ├── java/cn/arrebyte/ai/arrebyteai
│       └── resources/prompts
└── local-test.properties
```

关键模块：

- `ArrebyteAiApplication`：Spring Boot 启动入口。
- `ChatConfiguration`：创建 `openAiChatClient` 与 `anthropicChatClient`。
- `AiChatController`：提供聊天 HTTP 接口。
- `application.properties`：应用名、OpenAI、Anthropic 等基础配置。
- `src/test/resources/prompts`：测试用的系统提示词模板和图片资源。

## 环境要求

本地需要安装 Java 17。项目已包含 Maven Wrapper，可以直接使用 `./mvnw` 执行构建和启动命令。

```bash
java -version
./mvnw -version
```

## 配置

应用默认导入根目录下的 `local-test.properties`：

```properties
spring.config.import=optional:file:./local-test.properties
```

`local-test.properties` 已被 `.gitignore` 忽略，适合放本地密钥和模型服务地址。可以使用环境变量，也可以在 `local-test.properties` 中覆盖 Spring 配置。

通过环境变量配置：

```bash
export OPENAI_API_KEY=your-openai-api-key
export OPENAI_BASE_URL=http://127.0.0.1:8317
export OPENAI_CHAT_COMPLETIONS_PATH=/v1/chat/completions
export OPENAI_MODEL=gpt-5.5
export ANTHROPIC_API_KEY=your-anthropic-api-key
```

或通过 `local-test.properties` 配置：

```properties
spring.ai.openai.api-key=your-openai-api-key
spring.ai.openai.chat.base-url=http://127.0.0.1:8317
spring.ai.openai.chat.completions-path=/v1/chat/completions
spring.ai.openai.chat.options.model=gpt-5.5
spring.ai.anthropic.api-key=your-anthropic-api-key
```

默认配置中 OpenAI 服务地址为 `http://127.0.0.1:8317`，模型名为 `gpt-5.5`。如果使用真实 OpenAI、兼容 OpenAI 协议的代理服务或其他本地网关，请按实际服务调整。

## 启动

```bash
./mvnw spring-boot:run
```

默认启动后监听 `http://localhost:8080`。

## 接口

所有接口请求体格式一致：

```json
{
  "message": "请用中文介绍 Spring AI"
}
```

响应格式：

```json
{
  "content": "模型返回内容"
}
```

### OpenAI 聊天

```bash
curl -X POST http://localhost:8080/api/ai/openai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"请用中文介绍 Spring AI"}'
```

### OpenAI 流式调用

```bash
curl -X POST http://localhost:8080/api/ai/openai/stream/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"请用中文介绍 Spring AI"}'
```

当前接口会在服务端收集流式响应内容，并以普通 JSON 结果返回。

### Anthropic 聊天

```bash
curl -X POST http://localhost:8080/api/ai/anthropic/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"请用中文介绍 Spring AI"}'
```

如果 `message` 为空或只包含空白字符，接口会返回 `400 Bad Request`。

## 测试

编译项目：

```bash
./mvnw -DskipTests compile
```

运行测试：

```bash
./mvnw test
```

注意：当前测试会创建真实 `ChatClient` 并调用模型能力，部分用例依赖可用的 OpenAI/Anthropic 配置、模型服务和测试资源。没有配置密钥或本地模型代理时，完整测试可能失败。

主要测试内容：

- `ArrebyteAiApplicationTests`：校验 Spring 上下文、`ChatClient` Bean 和系统提示词用法。
- `OpenAiChatSysPromptsTest`：使用 `SystemPromptTemplate` 读取提示词模板。
- `MultiModalTest`：使用 `att.png` 演示 OpenAI 多模态请求。

## 开发说明

- 新增模型提供方时，优先在 `ChatConfiguration` 中集中创建对应的 `ChatClient` Bean。
- 新增 HTTP 能力时，优先在 `AiChatController` 或独立 Controller 中保持清晰的接口边界。
- 本地密钥、代理地址和模型名不要提交到 Git，放入 `local-test.properties` 或环境变量。
