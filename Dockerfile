FROM fabric8/java-centos-openjdk8-jdk:1.6

ENV AB_ENABLED off
ENV AB_JOLOKIA_AUTH_OPENSHIFT true
ENV JAVA_OPTIONS -Xmx256m 

EXPOSE 8080

RUN chmod -R 777 /deployments/
ADD target/mapit-spring.jar /deployments/