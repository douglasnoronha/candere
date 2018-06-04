#!/bin/bash

yarn build
mv build/static/css build/css
mv build/static/js build/js
rm -rf build/static
echo "make sure to update Webpack paths in html file & zip, too lazy to add it to script"