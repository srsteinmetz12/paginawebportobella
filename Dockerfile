FROM amazoncorretto:17-alpine
WORKDIR /app
COPY . /app/
RUN find src -name "*.java" > sources.txt
RUN javac -cp "lib/*" -d classes @sources.txt
EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]