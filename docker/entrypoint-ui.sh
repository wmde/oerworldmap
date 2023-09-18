#!/bin/bash

echo "INSTALL"
npm install
echo "BUILD PRDO"
npm run build:prod
echo "SERVER PROD"
npm run server:prod
# does not use the right host:port
#echo "BUILD DEV"
#npm run build:dev
#echo "SERVER DEV"
#npm run server:dev
