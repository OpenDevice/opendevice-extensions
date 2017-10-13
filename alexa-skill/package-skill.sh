#!/bin/bash

cd $(dirname $0)

cp AlexaSkill.js index.js
zip -r skill.zip node_modules index.js
rm index.js
