#!/bin/bash

set -euox pipefail


source credentials

id=${1:-"zell"}
generation=${2:-"Zeebe 0.24.4"}
var=$(jq -n --arg id "$id" \
      --arg generation "$generation" \
      '{id: $id, generation: $generation}')

zbctl create instance daily-test-protocol --variables "$var"
