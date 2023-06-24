FROM gradle:7.4-jdk17-alpine as builder

WORKDIR /build
COPY ./ /build

RUN gradle shadowjar

FROM amazoncorretto:17-alpine3.12

WORKDIR /home/TicketTools
COPY --from=builder /build/build/libs/*.jar /TicketTools.jar
VOLUME /home/TicketTools/config.yml

ENTRYPOINT ["java","--enable-preview","-jar","/TicketTools.jar"]