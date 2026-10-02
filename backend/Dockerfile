FROM eclipse-temurin:17-jre-noble

WORKDIR /app

RUN apt-get update \
    && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 secureops

COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN chown secureops:secureops /app/app.jar

USER secureops

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]
