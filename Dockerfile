FROM gradle:8.6-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle --no-daemon shadowJar -x checkstyleMain -x checkstyleTest


FROM eclipse-temurin:11-jdk-jammy

RUN addgroup --system javauser && useradd -g javauser javauser

COPY --from=builder --chown=javauser:javauser /home/gradle/src/build/libs/medtechchain.jar /app/chaincode.jar
COPY --from=builder --chown=javauser:javauser /home/gradle/src/docker-entrypoint.sh /app/docker-entrypoint.sh

WORKDIR /app

RUN chmod +x /app/docker-entrypoint.sh

ENV PORT 9999
EXPOSE 9999

USER javauser
ENTRYPOINT [ "/app/docker-entrypoint.sh" ]