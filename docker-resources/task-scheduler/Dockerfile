FROM openjdk:8-jdk-alpine
LABEL maintainer="YangLong<yang_long@leapmotor.com>"
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mkdir /spring
WORKDIR /spring
COPY sia-task-scheduler/target/sia-task-scheduler.jar /spring/app.jar
COPY sia-task-build-component/config/task_scheduler_open.yml /spring/application-open.yml
COPY docker-resources/task-scheduler/entrypoint.sh /spring/entrypoint.sh
RUN mkdir logs
ENTRYPOINT ["/spring/entrypoint.sh","-Dspring.config.location=application-open.yml","/spring/app.jar"]