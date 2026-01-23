FROM openjdk:8u102-jre
COPY adhd-server/target/adhd-server.jar ./adhd-server.jar
ENTRYPOINT  [{ENTRYPOINT}]
