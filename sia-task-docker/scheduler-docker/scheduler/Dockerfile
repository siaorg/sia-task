FROM centos:latest
MAINTAINER L-craftsman

# add third part
ADD ./third-library/  /opt/

# add yum
RUN  curl -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo \
    && rpm -ivh http://nginx.org/packages/centos/7/noarch/RPMS/nginx-release-centos-7-0.el7.ngx.noarch.rpm \
    && rpm -ivh /opt/tcl-8.5.13-8.el7.x86_64.rpm \
    && rpm -ivh /opt/tcl-devel-8.5.13-8.el7.x86_64.rpm \
    && yum clean all \
    && yum makecache \
    && yum install -y gcc \
                    gcc-c++ \
                    glibc* \
                    automake \
                    autoconf \
                    libtool \
                    make \
                    libxml2-devel \
                    pcre-devel \
                    openssl \
                    openssl-devel \
                    libicu-devel \
                    file libaio \
                    libaio-devel \
                    libXext \
                   # libmcrypt \
                   # libmcrypt-devel \
                    numactl \
                    unzip \
                    zip \
                   # groupinstall \
                    #chinese-support \
                    #vixie-cron \
                    crontabs  \
                    telnet-server  \
                    telnet.*  \
                    java-1.8.0-openjdk \
                    lsof \
                    sudo \
                    nginx

# language setting
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8
ENV LC_ALL "zh_CN.UTF-8"

ADD ./jarPackage/sia-task-config-1.0.0.jar  /app/jar/ROOT/jarPackage/
ADD ./jarPackage/sia-task-scheduler-1.0.0.jar  /app/jar/ROOT/jarPackage/
ADD ./jarPackage/dist/ /app/jar/ROOT/dist/
ADD ./jarConfig/ /app/jar/ROOT/jarConfig/

RUN     cd  /app/jar/ROOT \
        && cp /opt/docker-start-scheduler.sh  /app/jar/ROOT \
        && chmod +x docker-start-scheduler.sh \

