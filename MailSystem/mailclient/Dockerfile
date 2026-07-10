FROM caddy:2-alpine

COPY mailclient/Caddyfile /etc/caddy/Caddyfile

# Use already-built frontend artifacts from the project root build/install folder.
COPY ../build/install/mailclient/ /srv/

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

EXPOSE 80
