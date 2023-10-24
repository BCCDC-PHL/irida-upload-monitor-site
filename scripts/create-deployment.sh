#!/bin/bash

rm -r target/deploy/irida-upload-monitor 2> /dev/null
mkdir -p target/deploy/irida-upload-monitor
mkdir -p target/deploy/irida-upload-monitor/js
cp -r resources/public/css target/deploy/irida-upload-monitor
cp -r resources/public/images target/deploy/irida-upload-monitor
cp target/public/cljs-out/prod/main_bundle.js target/deploy/irida-upload-monitor/js/main.js
cp resources/public/index_prod.html target/deploy/irida-upload-monitor/index.html
cp resources/public/favicon.ico target/deploy/irida-upload-monitor/favicon.ico
pushd target/deploy > /dev/null
tar -czf irida-upload-monitor.tar.gz irida-upload-monitor
rm -r irida-upload-monitor
popd > /dev/null
