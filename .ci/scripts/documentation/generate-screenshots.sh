#!/bin/sh -eux

# assumes that Nodejs and bpmn-to-image is installed

cd ../../../workflows

for filename in *.bpmn; do
    echo "${filename}"

    bpmn-to-image "$filename":"${filename%.bpmn}.png"
done

mv *.png ../docs/assets


