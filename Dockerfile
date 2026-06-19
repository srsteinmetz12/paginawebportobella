FROM amazoncorretto:17-alpine
WORKDIR /app
COPY src/ /app/src/
COPY lib/ /app/lib/
RUN find src -name "*.java" > sources.txt
RUN cat sources.txt  # Verifica se o arquivo PagamentoServer.java está na lista
RUN javac -cp "lib/*" -d classes src/util/PagamentoServer.java
RUN ls -la classes/util/  # Verifica se a classe foi compilada
EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]