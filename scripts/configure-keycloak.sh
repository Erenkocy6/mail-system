#!/usr/bin/env sh
set -eu

KEYCLOAK_SERVICE="${KEYCLOAK_SERVICE:-keycloak}"
KEYCLOAK_ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-mail-system}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-mail-client}"
KEYCLOAK_DEMO_USERNAME="${KEYCLOAK_DEMO_USERNAME:-aallanson@example.com}"
KEYCLOAK_DEMO_PASSWORD="${KEYCLOAK_DEMO_PASSWORD:-test1234}"

docker compose exec -T \
    -e KC_ADMIN_USER="$KEYCLOAK_ADMIN_USER" \
    -e KC_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD" \
    -e KC_REALM="$KEYCLOAK_REALM" \
    -e KC_CLIENT_ID="$KEYCLOAK_CLIENT_ID" \
    -e KC_DEMO_USERNAME="$KEYCLOAK_DEMO_USERNAME" \
    -e KC_DEMO_PASSWORD="$KEYCLOAK_DEMO_PASSWORD" \
    "$KEYCLOAK_SERVICE" sh <<'KEYCLOAK_SCRIPT'
set -eu

KC=/opt/keycloak/bin/kcadm.sh

$KC config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user "$KC_ADMIN_USER" \
    --password "$KC_ADMIN_PASSWORD" >/dev/null

# The Codex in-app browser can reach localhost through a proxy IP. For local
# development we explicitly allow HTTP, otherwise Keycloak rejects the request
# with "HTTPS required" even on localhost.
$KC update realms/master -s sslRequired=none

if $KC get "realms/$KC_REALM" >/dev/null 2>&1; then
    $KC update "realms/$KC_REALM" -s enabled=true -s sslRequired=none
else
    $KC create realms -s "realm=$KC_REALM" -s enabled=true -s sslRequired=none
fi

cat > /tmp/mail-client.json <<JSON
{
  "clientId": "$KC_CLIENT_ID",
  "name": "Mail Client",
  "enabled": true,
  "protocol": "openid-connect",
  "publicClient": true,
  "standardFlowEnabled": true,
  "implicitFlowEnabled": false,
  "directAccessGrantsEnabled": false,
  "serviceAccountsEnabled": false,
  "redirectUris": [
    "http://localhost:4200",
    "http://localhost:4200/*",
    "http://localhost:8081",
    "http://localhost:8081/app/",
    "http://localhost:8081/*"
  ],
  "webOrigins": ["+"],
  "rootUrl": "http://localhost:8081/",
  "baseUrl": "http://localhost:8081/",
  "attributes": {
    "pkce.code.challenge.method": "S256",
    "post.logout.redirect.uris": "http://localhost:4200##http://localhost:4200/*##http://localhost:8081##http://localhost:8081/app/##http://localhost:8081/*"
  }
}
JSON

CLIENT_UUID=$(
    $KC get clients -r "$KC_REALM" -q "clientId=$KC_CLIENT_ID" --fields id --format csv --noquotes |
        head -n 1 ||
        true
)

if [ -n "$CLIENT_UUID" ]; then
    $KC update "clients/$CLIENT_UUID" -r "$KC_REALM" -f /tmp/mail-client.json
else
    $KC create clients -r "$KC_REALM" -f /tmp/mail-client.json >/dev/null
fi

USER_UUID=$(
    $KC get users -r "$KC_REALM" -q "username=$KC_DEMO_USERNAME" --fields id --format csv --noquotes |
        head -n 1 ||
        true
)

if [ -z "$USER_UUID" ]; then
    $KC create users -r "$KC_REALM" \
        -s "username=$KC_DEMO_USERNAME" \
        -s "email=$KC_DEMO_USERNAME" \
        -s firstName=Ameline \
        -s lastName=Allanson \
        -s enabled=true \
        -s emailVerified=true >/dev/null
fi

$KC set-password -r "$KC_REALM" --username "$KC_DEMO_USERNAME" --new-password "$KC_DEMO_PASSWORD" --temporary=false

echo "Configured Keycloak realm '$KC_REALM', OIDC client '$KC_CLIENT_ID', and demo user '$KC_DEMO_USERNAME'."
KEYCLOAK_SCRIPT
