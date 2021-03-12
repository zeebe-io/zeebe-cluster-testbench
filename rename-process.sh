#!/bin/bash

set -euo pipefail

ENGINE_PATTERN='(?![-\s]+(?i)engine)'
REPLACE_PATTERN="s/WORKFLOWS([\b-_\s])/WORKFLOWES\$1/g; s/(?i)(workflow)(?-i)s/\$1es/g; s/WORKFLOW${ENGINE_PATTERN}/PROCESS/g; s/w(?i)orkflow${ENGINE_PATTERN}/process/g; s/W(?i)orkflow${ENGINE_PATTERN}/Process/g; s/([Pp])rocesskey/\$1rocessDefinitionKey/gi;"

for file in $(find . -type d -iname '*workflow*' -not -path '.*/node_modules/*' -not -path '.*/target/*' -not -path '.*/.git/*' -not -path '.*/.github/*' -not -path '.*/vendor/*')
do
	new_file=$(echo ${file} | perl -wpl -e "${REPLACE_PATTERN}")
	if [ "${file}" != "${new_file}" ]; then
		mv -v ${file} ${new_file}
	fi
done

for file in $(find . -type f -iname '*workflow*' -not -path '*/node_modules/*' -not -path '.*/target/*' -not -path '.*/.git/*' -not -path '.*/.github/*' -not -path '.*/vendor/*')
do
	new_file=$(echo ${file} | perl -wpl -e "${REPLACE_PATTERN}")
	if [ "${file}" != "${new_file}" ]; then
		mv -v ${file} ${new_file}
	fi
done

git add -A
git commit -anm 'chore(project): rename workflow to process in filenames'

grep --exclude-dir={.git,node_modules,target,.ci,.github,vendor} -i -P "workflow${ENGINE_PATTERN}" -r . -l | xargs -n1 perl -i -wpl -e "${REPLACE_PATTERN}"

git add -A
git commit -anm 'chore(project): rename workflow to process in files'
