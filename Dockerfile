FROM amazoncorretto:17-alpine
WORKDIR /app

# Copia tudo para dentro do container
COPY . /app/

# Compila todos os arquivos .java (incluindo subpastas)
RUN find . -name "*.java" > sources.txt
RUN javac -cp "lib/*" -d classes @sources.txt

EXPOSE 8080
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]