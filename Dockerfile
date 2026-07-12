FROM amazoncorretto:17-alpine

# ✅ INSTALA O GIT (SOLUÇÃO DO SEU PROBLEMA)
RUN apk add --no-cache git

WORKDIR /app
COPY src/ /app/src/
COPY lib/ /app/lib/

# 🔥 COMPILA SÓ OS PACOTES QUE EXISTEM NO SERVIDOR
RUN javac -cp "lib/*" -d classes $(find src/util src/connection src/paginaweb -name "*.java")

RUN ls -la classes/util/   # Verifica se a classe PagamentoServer.class foi gerada
RUN ls -la classes/        # Verifica a estrutura geral
EXPOSE 8080
RUN ls -la /app/classes/util/PagamentoServer.class
RUN javap -cp classes util.PagamentoServer | grep main
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]