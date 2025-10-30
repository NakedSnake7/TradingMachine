# Usamos la imagen de OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Directorio de la app
WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Ejecutamos Maven Wrapper para compilar
RUN ./mvnw clean package -DskipTests

# Puerto que escuchará la app
EXPOSE 8080

# Comando para ejecutar el jar generado
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]

