FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN if [ -f target/*.jar ]; then \
      echo "JAR ya presente"; \
    else \
      chmod +x mvnw 2>/dev/null || true; \
      ./mvnw clean package -DskipTests || mvn clean package -DskipTests; \
    fi

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY rabbitmq-ca.pem /tmp/rabbitmq-ca.pem
RUN if [ -f /tmp/rabbitmq-ca.pem ]; then \
      keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt -alias styleradar-rabbitmq-ca -file /tmp/rabbitmq-ca.pem && rm -f /tmp/rabbitmq-ca.pem; \
    fi
COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8260
ENTRYPOINT ["java", "-jar", "app.jar"]
