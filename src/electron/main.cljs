(ns electron.main
  (:require ["fs" :as fs]
            ["path" :as path]
            ["electron" :refer [BrowserWindow app] :as electron]))

(defonce prod? (= js/process.env.NODE_ENV "production"))
(defonce dev? (not prod?))

(def ROOT_PATH (path/join js/__dirname ".."))
(def ASSETS_ROOT_PATH (path/join ROOT_PATH "assets"))
(def MAIN_PROD_WINDOW_ENTRY (str "file://" (path/join js/__dirname "../index.html")))

(defn create-main-window
  "create main app window"
  []
  (let [win-opts {:width  900
                  :height 700
                  :webPreferences
                          {:nodeIntegration false}}
        url (if dev? "http://localhost:8080" MAIN_PROD_WINDOW_ENTRY)
        win (BrowserWindow. (clj->js win-opts))]
    (.loadURL win url)
    (when dev? (.. win -webContents (openDevTools)))))

(defn -main
  "main"
  []
  (.on app "ready"
       (fn []
         (create-main-window))))