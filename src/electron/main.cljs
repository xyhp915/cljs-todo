(ns electron.main
  (:require ["fs" :as fs]
            ["path" :as path]
            ["electron" :refer [BrowserWindow app Menu dialog autoUpdater] :as electron]))

(defonce mac? (= (.-platform js/process) "darwin"))
(defonce win32? (= (.-platform js/process) "win32"))

(defonce prod? (= js/process.env.NODE_ENV "production"))
(defonce dev? (not prod?))
(defonce log (js/require "electron-log"))
(defonce updater (js/require "../updater.js"))

(def ROOT_PATH (path/join js/__dirname ".."))
(def ASSETS_ROOT_PATH (path/join ROOT_PATH "assets"))
(def MAIN_PROD_WINDOW_ENTRY (str "file://" (path/join js/__dirname "../index.html")))

;; Handle creating/removing shortcuts on Windows when installing/uninstalling.
(when (js/require "electron-squirrel-startup") (.quit app))

(defn setup-menu!
  "setup global menu"
  []
  (let [menu (. Menu buildFromTemplate (clj->js (-> []
                                                    (conj {:label   "ToDo™ App"
                                                           :submenu [{:label "check for update"
                                                                      :id    "update"
                                                                      :click updater.checkForUpdates}]}))))]
    (. Menu setApplicationMenu menu)))

(defn create-main-window
  "create main app window"
  []
  (let [win-opts {:width         980
                  :height        700
                  :titleBarStyle (if mac? "hidden" nil)
                  :frame false
                  :webPreferences
                  {:nodeIntegration         false
                   :nodeIntegrationInWorker false
                   :contextIsolation        true
                   :preload                 (str ROOT_PATH "/preload.js")}}
        url (if dev? "http://localhost:8080" MAIN_PROD_WINDOW_ENTRY)
        win (BrowserWindow. (clj->js win-opts))]
    ;(setup-menu!)
    (.loadURL win url)
    ;(when dev? (.. win -webContents (openDevTools)))
    win))

(defn setup-updater! [notify-update-status]
  ;; updater logging
  (set! (.. autoUpdater -logger) log)
  (set! (.. autoUpdater -logger -transports -file -level) "info")
  ;;(set! (.. autoUpdater -channel) "beta")
  ;
  (.. log (info (str "ToDo™ App(" (.getVersion app) ") Starting... ")))

  (let [init-updater (js/require "update-electron-app")]
    (init-updater #js {:repo           "xyhp915/cljs-todo"
                       :updateInterval "1 hour"
                       :logger         log}))
  ;;; updater hooks
  ;(doto autoUpdater
  ;  (.on "checking-for-update" #(notify-update-status "checking for updating..."))
  ;  (.on "update-not-available" #(notify-update-status "update not available"))
  ;  (.on "error" #(notify-update-status %))
  ;  (.on "download-progress"
  ;       #(let [progress-clj (js->clj %)
  ;              {:keys [percent transferred total]} progress-clj
  ;              msg (str "Progress Downloaded " percent "%"
  ;                       " (" transferred "/" total ")")]
  ;          (notify-update-status msg)))
  ;  (.on "update-downloaded" #(do (notify-update-status "update downloaded")
  ;                                (.. autoUpdater quitAndInstall)))
  ;  (.checkForUpdatesAndNotify))
)

(defn -main
  []
  (.on app "window-all-closed" #(when-not mac? (.quit app)))
  (.on app "ready"
       (fn []
         (let [^js win (create-main-window)
               *win (atom win)
               *quitting? (atom false)]

           ;; auto updater
           (setup-updater! nil)

           ;; main window events
           (.on win "close" #(if (or @*quitting? win32?)
                               (reset! *win nil)
                               (do (.preventDefault ^js/Event %)
                                   (.hide win))))
           (.on app "before-quit" #(reset! *quitting? true))
           (.on app "activate" #(if @*win (.show win)))))))