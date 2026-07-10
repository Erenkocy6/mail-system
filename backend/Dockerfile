FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SERVER_PORT=8080
ENV FILE_UPLOAD_DIR=/app/uploads

COPY build/install/backend/ /app/

RUN chmod +x /app/bin/backend \
    && mkdir -p /app/uploads \
    && addgroup --system mailserver \
    && adduser --system --ingroup mailserver --no-create-home mailserver \
    && chown -R mailserver:mailserver /app

USER mailserver

EXPOSE 8080

CMD ["/app/bin/backend"]
