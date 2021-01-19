dev:
	lein shadow-cljs-dev

build:
	lein shadow-cljs-compile

release:
	lein shadow-cljs-release

build-electron-main-dev:
	lein shadow-cljs watch electron-main

build-electron-main:
	lein shadow-cljs compile electron-main

release-electron-main:
	lein shadow-cljs release electron-main

electron-dev-main:
	cd public/ && yarn run electron:dev

electron-release-darwin: release-electron-main release
	cd public/ && yarn run electron:make:darwin

electron-release-win32:
	cd public/ && yarn run electron:make:win32

electron-publish-github:
	cd public/ && yarn run electron:publish:github