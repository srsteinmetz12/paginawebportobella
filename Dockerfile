FROM amazoncorretto:17-alpine
WORKDIR /app

# Copia a pasta src e lib (ajuste o caminho)
COPY Brecho_portobella/src/ /app/src/
COPY Brecho_portobella/lib/ /app/lib/

# Compila todos os arquivos .java
RUN find src -name "*.java" > sources.txt
RUN javac -cp "lib/*" -d classes @sources.txt

EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]