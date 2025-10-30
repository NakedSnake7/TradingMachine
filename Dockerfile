# Usamos la imagen de OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de la app
WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Etapa 1: Build con Maven
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiamos solo los archivos necesarios para Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Damos permisos al wrapper de Maven
RUN chmod +x mvnw

# Construimos la aplicación (omitimos tests para acelerar)
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen final más ligera
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiamos solo el JAR desde la etapa build
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Puerto que escuchará la app
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]

