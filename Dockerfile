# Etapa 1: Build y empaquetado
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN if [ -f target/*.jar ]; then \
      echo "JAR ya presente"; \
    else \
      chmod +x mvnw 2>/dev/null || true; \
      ./mvnw clean package -DskipTests || mvn clean package -DskipTests; \
    fi

# Etapa 2: Imagen final ligera de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8260
ENTRYPOINT ["java", "-jar", "app.jar"]
