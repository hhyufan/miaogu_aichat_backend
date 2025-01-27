# 使用 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 将 pom.xml 和源代码复制到容器中
COPY pom.xml .
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests

# 将构建的 jar 文件复制到容器中
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
