FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia arquivos do Maven
COPY backend-java/pom.xml .
COPY backend-java/src ./src

# Instala Maven e compila o projeto
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Estágio de execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/financehub-core-*.jar /app/app.jar
COPY --from=build /app/target/classes /app/classes

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-Djava.awt.headless=true", "-jar", "/app/app.jar"]

