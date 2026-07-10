#!/usr/bin/env bash

COMMAND=(
  "snort"
  "-c" "/etc/snort/snort.lua"
  # "--rule-path" "/etc/snort/snort3-community-rules/snort3-community.rules"
  # "-u" "snort"
  # "-g" "snort"
  # do not filter packets with not yet valid checksums due to offloading
  # see: https://www.snort.org/faq/i-m-not-receiving-alerts-in-snort
  "-k" "none"
  # full alerting
  "-A" "alert_full"
)

echo "Executing: ${COMMAND[*]}"
exec "${COMMAND[@]}"
