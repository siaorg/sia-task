FROM openjdk:8-jdk-alpine
LABEL maintainer="YangLong<yang_long@leapmotor.com>"
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mkdir /spring
WORKDIR /spring
COPY sia-task-executor-demo/target/sia-task-executor-demo.jar /spring/app.jar
COPY docker-resources/task-demo/entrypoint.sh /spring/entrypoint.sh
RUN mkdir logs
ENTRYPOINT ["/spring/entrypoint.sh","/spring/app.jar"]