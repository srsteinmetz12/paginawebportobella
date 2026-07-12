FROM amazoncorretto:17-alpine

# ✅ INSTALA O GIT
RUN apk add --no-cache git

WORKDIR /app
COPY src/ /app/src/
COPY lib/ /app/lib/
# 🔥 NÃO COPIA config.properties - usa variáveis de ambiente

# 🔥 COMPILA SÓ OS PACOTES QUE EXISTEM NO SERVIDOR
RUN javac -cp "lib/*" -d classes $(find src/util src/connection src/paginaweb -name "*.java")

RUN ls -la classes/util/
RUN ls -la classes/
EXPOSE 8080
RUN ls -la /app/classes/util/PagamentoServer.class
RUN javap -cp classes util.PagamentoServer | grep main
CMD ["java", "-cp", "lib/*:classes", "util.PagamentoServer"]