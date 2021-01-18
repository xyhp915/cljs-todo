(defproject app "0.1.0-SNAPSHOT"
  :aliases
  {"shadow-cljs"         ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli"]
   "shadow-cljs-dev"     ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]
   "shadow-cljs-compile" ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "compile" "app"]
   "shadow-cljs-release" ["with-profile" "+cljs" "run" "-m" "shadow.cljs.devtools.cli" "release" "app"]}

  :dependencies
  [[org.clojure/clojure "1.10.1"]
   [org.clojure/clojurescript "1.10.758"]
   [tupelo "20.07.28"]
   [reagent "0.10.0"]
   [rum "0.12.3"]
   [com.google.javascript/closure-compiler-unshaded "v20200719"]]

  :profiles
  {:cljs
   {:dependencies
    [[thheller/shadow-cljs "2.10.19"]
     [cider/piggieback "0.4.0"]]}})