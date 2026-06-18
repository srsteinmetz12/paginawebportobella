FROM adoptopenjdk:11-jdk-hotspot
WORKDIR /app
COPY lib/ /app/lib/
COPY classes/ /app/classes/
EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]