FROM amazoncorretto:17-alpine
WORKDIR /app
COPY src/ /app/src/
COPY lib/ /app/lib/

# 🔥 COMPILA TUDO EXCETO views e desktop
RUN javac -cp "lib/*" -d classes $(find src -name "*.java" ! -path "src/views/*" ! -path "src/desktop/*")

RUN ls -la classes/util/   # Verifica se a classe PagamentoServer.class foi gerada
RUN ls -la classes/        # Verifica a estrutura geral
EXPOSE 8080
RUN ls -la /app/classes/util/PagamentoServer.class
RUN javap -cp classes util.PagamentoServer | grep main
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]