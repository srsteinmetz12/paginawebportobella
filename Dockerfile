FROM amazoncorretto:17-alpine
WORKDIR /app
COPY lib/ /app/lib/
COPY src/ /app/src/
RUN mkdir -p /app/classes
RUN javac -cp "lib/*" -d classes src/util/*.java
EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]