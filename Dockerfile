ARG HOSPITAL_DOMAIN

FROM gradle:8.6-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src

RUN test -d /home/gradle/src/crypto/$HOSPITAL_DOMAIN # "Crypto material not provided"

WORKDIR /home/gradle/src

RUN gradle --no-daemon shadowJar -x checkstyleMain -x checkstyleTest


FROM eclipse-temurin:11-jdk-jammy
ARG CC_SERVER_PORT=9999

# Setup tini to work better handle signals
ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN addgroup --system javauser && useradd -g javauser javauser

COPY --from=builder --chown=javauser:javauser /home/gradle/src/build/libs/medtechchain.jar /app/chaincode.jar
COPY --from=builder --chown=javauser:javauser /home/gradle/src/docker/docker-entrypoint.sh /app/docker-entrypoint.sh
COPY --from=builder --chown=javauser:javauser /home/gradle/src/crypto/$HOSPITAL_DOMAIN /crypto/$HOSPITAL_DOMAIN

WORKDIR /app

RUN chmod +x /app/docker-entrypoint.sh

ENV PORT $CC_SERVER_PORT

EXPOSE $CC_SERVER_PORT

USER javauser
ENTRYPOINT [ "/tini", "--", "/app/docker-entrypoint.sh" ]