(ns todo.core
  (:require
    [rum.core :as rum]))

(def app-cache-key "__todo__cljs__")

;;; helpers ;;;;
(defn restore-app-state []
  (let [app-state (js->clj
                    (js/JSON.parse (js/localStorage.getItem app-cache-key))
                    :keywordize-keys true)]
    (if (vector? app-state) app-state [])))

;;; states ;;;;
(def *state
  (atom
    {:items   (restore-app-state)
     :version "0.0.1"}))

(add-watch *state :items
           (fn [k a o n]
             (let [cache-str (js/JSON.stringify (clj->js (:items n)))]
               (js/console.log "changed")
               (js/localStorage.setItem app-cache-key cache-str))))

;;;; handlers ;;;;
(defn resolve-item-position [identity]
  (first (keep-indexed (fn [index it]
                         (if (= (:id it) identity) index))
                       (:items @*state))))

(defn gen-default-todo
  [text]
  {:id (inc (js/Date.now)) :text text :checked false :editing false})

(defn fix-item-identities []
  (let [{:keys [items]} @*state]
    (swap! *state assoc :items
           (into []
                 (map-indexed
                   (fn [k v]
                     (js/console.log k v)
                     (assoc v :id k)) items)))))

(defn map-items
  [it-handler]
  (let [{:keys [items]} @*state]
    (swap! *state #(assoc @*state :items (into [] (map it-handler items))))))

(defn on-add-item [input]
  (when-not (clojure.string/blank? input)
    (swap! *state update :items conj (gen-default-todo input))))

(defn on-toggle-all [checked?]
  (map-items #(assoc % :checked checked?)))

(defn on-toggle-item [identity checked]
  (let [pos (resolve-item-position identity)]
    (swap! *state assoc-in [:items pos :checked] checked)))

(defn on-remove-item [identity]
  (swap! *state update :items (fn [items] (remove #(= (:id %) identity) items)) (:items @*state)))

(defn check-has-item-completed? []
  (some #(true? (:checked %)) (:items @*state)))

(defn on-clear-completed []
  (if (check-has-item-completed?)
    (let [{:keys [items]} @*state]
      (swap! *state assoc :items (filterv #(not (:checked %)) items)))))

;;; TODO App components ;;;;
(rum/defc app-header
  []
  [:header.header
   [:h1 "todos"]
   [:input.new-todo
    {:placeholder   "what needs to be done ?"
     :autoFocus     true
     :default-value ""
     :on-key-down   #(if (= (.-key %) "Enter")
                       (let [target (. % -target)]
                         (do
                           (on-add-item (. target -value))
                           (set! (. target -value) ""))))
     }]])

(rum/defc todo-list-item < rum/static
  [identity text checked editing]
  [:li
   {:class [(if checked "completed")
            (if editing "editing")]}
   [:div.view
    [:input.toggle {:type      "checkbox"
                    :checked   checked
                    :on-change #(on-toggle-item identity (.. % -target -checked))}]
    [:label (str "" text)]
    [:button.destroy {:on-click #(on-remove-item identity)
                      }]]
   [:input.edit {:default-value text}]])

(rum/defc app-content
  [items]
  (let [is-not-all-checked
        (if (not= 0 (count items))
          (some #(false? (:checked %)) items)
          true)]
    (js/console.log is-not-all-checked)
    [:section.main
     [:input#toggle-all.toggle-all
      {:type      "checkbox"
       :on-change #(on-toggle-all (.. % -target -checked))
       :checked   (not is-not-all-checked)}]
     [:label {:for "toggle-all"} "Mark all as complete"]
     [:ul.todo-list
      (for [{:keys [id text checked editing]} items]
        (rum/with-key
          ;;; TODO: memorize function component
          (apply todo-list-item [id text checked editing])
          id))
      ]]))

(rum/defc app-footer
  [on-clear-completed item-size]
  [:footer.footer
   [:span.todo-count
    [:strong item-size]
    " item left"]
   (if (check-has-item-completed?)
     [:button.clear-completed
      {:on-click on-clear-completed}
      "Clear completed"])])

;;; root app
(rum/defcs app < rum/reactive
  []
  (let [{:keys [items]} (rum/react *state)]
    [:div.todoapp
     (app-header)
     (app-content items)
     (app-footer on-clear-completed (count items))])
  )

(defn get-container-el
  "get root container element"
  []
  (.getElementById js/document "root"))

;;; start ui
(defn start
  "start ui"
  []
  ;(rdom/render [-app] (get-container-el))
  (rum/mount (app) (get-container-el))
  )

(defn stop
  "stop ui"
  []
  (js/console.log "STOPPing ..."))

;; main
(defn -init
  ";;;app init entry;;;"
  []
  (start))

