dev:
	lein shadow-cljs-dev

build:
	lein shadow-cljs-compile

release:
	lein shadow-cljs-release

dev-electron-main-and-renderer:
	lein shadow-cljs watch app electron-main

release-electron-main-and-renderer: release
	lein shadow-cljs release electron-main

electron-dev-main:
	cd public/ && yarn run electron:dev

electron-release-app: release-electron-main-and-renderer
	cd public/ && yarn run electron:make

#electron-release-linux:
#	cd public/ && yarn run electron:make:linux

electron-publish-github:
	cd public/ && yarn run electron:publish:github