dev:
	lein shadow-cljs-dev

build:
	lein shadow-cljs-compile

build-electron-main-dev:
	lein shadow-cljs watch electron-main

build-electron-main:
	lein shadow-cljs compile electron-main


release-electron-main:
	lein shadow-cljs release electron-main

electron-dev-main:
	cd public/ && yarn run electron:dev

electron-release-darwin:
	cd public/ && yarn run electron:make:darwin

electron-release-win32:
	cd public/ && yarn run electron:make:win32