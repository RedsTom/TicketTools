FROM gradle:7.4-jdk17-alpine as builder

WORKDIR /build
COPY ./ /build

RUN gradle shadowjar

FROM amazoncorretto:17-alpine3.12

WORKDIR /home/GravenSupport
COPY --from=builder /build/build/libs/*.jar /GravenSupport.jar
VOLUME /home/GravenSupport/config.yml

ENTRYPOINT ["java","--enable-preview","-jar","/GravenSupport.jar"]