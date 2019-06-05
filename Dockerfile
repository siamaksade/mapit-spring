FROM docker.io/openjdk:8

ENV AB_ENABLED off
ENV AB_JOLOKIA_AUTH_OPENSHIFT true
ENV JAVA_OPTIONS -Xmx256m 

EXPOSE 8080

RUN mkdir /deployments/ \
  && chmod -R 777 /deployments/
ADD target/mapit-spring.jar /deployments/