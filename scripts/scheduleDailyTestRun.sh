#!/bin/bash


if [ -z "$1" ]
then
 echo "Please provide as first argument the id and second the generation you want to run with.
Example execution $0 'zell' 'Zeebe 0.24.4'"
 exit 1
fi

if [ -z "$2" ]
then
 echo "Please provide as first argument the id and second the generation you want to run with.
Example execution $0 'zell' 'Zeebe 0.24.4'"
 exit 1
fi


id=$1
generation=$2
var=$(jq -n --arg id "$id" \
      --arg generation "$generation" \
      '{id: $id, generation: $generation}')

zbctl create instance daily-test-protocol --variables "$var"
