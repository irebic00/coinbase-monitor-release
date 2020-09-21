# hadolint ignore=DL3007
FROM library/maven:latest as builder

WORKDIR /usr/src/app

ARG VERSION
ENV VERSION_ENV=${VERSION}

COPY . /usr/src/app/
# Takes a lot of time. Build it localy and copy jar from target/
# RUN mvn --batch-mode -Drevision=${VERSION_ENV} -DprocessAllModules=true test package
COPY monitor/target/monitor-${VERSION}.jar /usr/src/app


# hadolint ignore=DL3006
FROM adoptopenjdk/openjdk11
EXPOSE 8080

ARG VERSION
ENV VERSION_ENV=${VERSION}

COPY --from=builder /usr/src/app/monitor-${VERSION_ENV}.jar .

RUN groupadd -r func_user && useradd -r -s /bin/false -g func_user func_user

USER func_user
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar monitor-${VERSION_ENV}.jar"]