FROM amazoncorretto:17-alpine
WORKDIR /app
COPY src/ /app/src/
COPY lib/ /app/lib/
RUN javac -cp "lib/*" -d classes $(find src -name "*.java")
EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]