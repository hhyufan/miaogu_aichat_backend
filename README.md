# 喵咕聊天平台后端

欢迎来到**喵咕聊天平台后端**的食用指南(≧▽≦)！这里是一个充满可爱ACG风格的AI聊天平台后端部分。

## 📝 项目简介

喵咕聊天平台后端是一个基于Kotlin和Spring AI/Boot构建的AI聊天服务，使用MyBatis-Plus进行数据访问，提供高效、灵活的聊天功能 (｡•̀ᴗ-)✧。让你在聊天的同时享受可爱的ACG氛围(*≧ω≦)！

## 🚀 技术栈

- **Kotlin**: 使用Kotlin语言构建后端逻辑，语法简洁，类型安全。
- **Spring Boot**: 轻量级框架，快速构建应用程序。
- **Spring AI**: 用于AI聊天
- **MyBatis-Plus**: 方便的ORM框架，简化数据库操作。
- **MySQL**: 数据存储，使用关系型数据库管理系统。
- **Maven**: 项目管理与构建工具。

## 📦 安装与运行

1. **克隆项目**

   ```
   git clone https://github.com/hhyufan/miaogu_aichat_backend.git
   cd miaogu_aichat
   ```

2. **配置数据库**

   在`src/main/resources/application.yaml`中配置你的数据库连接信息：

   ```
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/miaogu_aichat?useSSL=false&serverTimezone=UTC
       username: root
       password: root
   ```

3. **初始化数据库**

   - 使用 `src/main/resources/sql/refactor_data.sql` 创建数据库和表（在MySQL命令行或数据库管理工具中执行）
   - 启动Springboot时，会自动执行sql文件，创建数据库和表

4. **AI 配置**
   在`src/main/resources`中创建名为`openai-config.yaml`的配置文件，并配置你的OpenAI API域名以及密钥：
   ```
   spring:
     ai:
       openai:
         chat:
           api-key: your-api-key
           base-url: your-api-base-url
   ```
5. **构建与运行**

   使用Maven构建项目并运行：

   ```
   mvn clean install
   mvn spring-boot:run
   ```

   服务器启动后，你将在SpringBoot打印看到类似以下的输出：

   ```
   Server is running on port: 8088
   ```

## 🌟 喵咕聊天平台 REST API 接口

可以通过`src/main/resources/api_test/apiTest.http`对接口进行测试，以下是一些示例接口调用：

- **获取所有聊天3.5消息**: `GET /1002/messages`

- **发送聊天3.5消息**: `POST /1002/send`

- **获取所有聊天4消息**: `GET /1003/messages`

- **发送聊天4消息**: `POST /1003/send`

- **清除聊天消息**: `DELETE /chat/clear`

- **获取好友列表**: `GET /friend/friendList`

- **用户登录**: `POST /user/login`

- **用户注册**: `POST /user/register`

## 🐾 贡献

欢迎任何形式的贡献 (๑•̀ㅂ•́)و！如果你有好的想法或发现了bug，请提交Issue或Pull Request(˘•ω•˘)◞⁽˙³˙⁾。

## 💖 感谢

感谢你对喵咕聊天平台的关注(｡・ω・｡)！希望你在这里找到乐趣，和AI小伙伴们一起畅聊吧ヽ( ^ω^ ゞ )！如果你喜欢这个项目，欢迎给我们星星⭐️！
