FROM openjdk:8u102-jre
COPY pdhd-server/target/pdhd-server.jar ./pdhd-server.jar
ENTRYPOINT  [{ENTRYPOINT}]
