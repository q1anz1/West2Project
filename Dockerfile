# 基础镜像
FROM openjdk:17
#作者
MAINTAINER qianz
#对外暴露的端口
EXPOSE 8080
#设定时区
ENV TZ=Asia/Shanghai


ADD West2Project-0.0.1-SNAPSHOT.jar /app/
#入口
ENTRYPOINT ["java","-jar", "/West2Project-0.0.1-SNAPSHOT.jar"]