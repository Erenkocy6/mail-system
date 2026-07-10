FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the server distribution (bin/ + lib/)
COPY ../build/install/mailserver/ /app/

# Make the start script executable and hand over /app to a dedicated runtime user
RUN chmod +x /app/bin/mailserver \
    && addgroup --system mailserver \
    && adduser --system --ingroup mailserver  \
      --no-create-home mailserver \
    && chown -R mailserver:mailserver /app

USER mailserver

ENV ATTACHMENT_DIR=/app/attachments

# Create a directory for
RUN mkdir ${ATTACHMENT_DIR}

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

EXPOSE 8080

CMD ["/app/bin/mailserver"]
