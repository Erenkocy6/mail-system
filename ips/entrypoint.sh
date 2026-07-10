#!/usr/bin/env bash

set -e
set -o pipefail

NETWORK_INTERFACE_NAME="eth0"
NETWORK_ADDRESS_CIDR=$(ip addr show "${NETWORK_INTERFACE_NAME}" | grep "inet\b" | awk '{print $2}')
NETWORK_ADDRESS=$(echo "${NETWORK_ADDRESS_CIDR}" | cut -d'/' -f1)

TARGET_ADDRESS=$(getent hosts "${FORWARD_HOST}" | awk '{print $1}')
FORWARD_HTTP_PORT="${FORWARD_HTTP_PORT:-8080}"

echo "Network address: ${NETWORK_ADDRESS}"
echo "Forward host: ${FORWARD_HOST}"
echo "Forward HTTP port: ${FORWARD_HTTP_PORT}"
echo "Forward HTTPS port: ${FORWARD_PORT}"
echo "Target address: ${TARGET_ADDRESS}"

for PORT in "${FORWARD_HTTP_PORT}" "${FORWARD_PORT}"; do
  iptables -t nat -A PREROUTING -p tcp --dport "${PORT}" -j DNAT --to-destination "${TARGET_ADDRESS}:${PORT}"
  iptables -t nat -A POSTROUTING -p tcp -d "${TARGET_ADDRESS}" --dport "${PORT}" -j SNAT --to-source "${NETWORK_ADDRESS}"
done

# redirect all forwarded packets to queue watched by snort
# (queue-num identifies the queue and has to match the snort "--daq-var queue=X" option)
iptables -I FORWARD -j NFQUEUE --queue-num=1

sed -i "s|__HOME_NET__|${TARGET_ADDRESS}/32|g" /etc/snort/snort.lua
sed -i "s|__HTTP_PORTS__|${FORWARD_PORT}|g" /etc/snort/snort.lua

cat /etc/snort/snort.lua

exec "$@"
