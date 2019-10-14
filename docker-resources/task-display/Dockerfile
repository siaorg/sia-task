FROM nginx:1.17.4-alpine
LABEL maintainer="YangLong<yang_long@leapmotor.com>"
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone