FROM centos:7

RUN yum install -y java-1.8.0-openjdk ruby-devel rubygems && yum groupinstall -y "Development Tools" && yum clean all
RUN gem install fpm

ENV SCALA_VERSION 2.11
ENV PILLAR_VERSION 2.3.0

COPY src/main/bash /opt/pillar/bin
RUN  chmod +x /opt/pillar/bin/*
COPY src/main/resources /opt/pillar/conf
COPY target/scala-${SCALA_VERSION}/pillar-assembly-${PILLAR_VERSION}.jar /opt/pillar/lib/pillar.jar

VOLUME /opt/pillar/conf

# Not adding PILLAR* env variables in order to avoid eclipsing application.conf

ENTRYPOINT ["/opt/pillar/bin/pillar"]
