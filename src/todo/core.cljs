(ns todo.core
  (:require
    [rum.core :as rum]))

(def app-cache-key "__todo__cljs__")

(defn -app
  "app component"
  []
  [:div
   [:h2 {:style {:color "blue"}} "i am app component--"]
   [:p "new-yet a new paragraph"]
   [:button {
             :on-click #(js/alert "hello-click")
             } "click"]]
  )

;;; TODO App components ;;;;
(rum/defc --header
  "TODO: header component"
  [{:keys [on-enter]}]
  [:header.header
   [:h1 "todos"]
   [:input.new-todo
    {:placeholder   "what needs to be done ?"
     :autoFocus     true
     :default-value ""
     :on-key-down   #(if (= (.-key %) "Enter")
                       (let [target (. % -target)]
                         (do
                           (js/setTimeout (fn [] (set! (. target -value) "")) 1)
                           (on-enter (. target -value)))))
     }]])

(rum/defc --todo-list-item
  [{:keys [text editing identity checked on-toggle on-remove-item]}]
  [:li
   {:class [(if checked "completed")
            (if editing "editing")]}
   [:div.view
    [:input.toggle {:type      "checkbox"
                    :checked   checked
                    :on-change #(if-not (nil? on-toggle) (on-toggle identity (.. % -target -checked)))}]
    [:label (str "" text)]
    [:button.destroy {:on-click #(on-remove-item identity)
                      }]]
   [:input.edit {:default-value text}]])

(rum/defc --content
  [items on-toggle-all on-toggle-item on-remove-item]
  (let [items-keys (range (count items))
        is-not-all-checked (if (not= 0 (count items))
                             (some #(false? (:checked %)) items)
                             true)]
    (js/console.log is-not-all-checked)
    [:section.main
     [:input#toggle-all.toggle-all
      {:type      "checkbox"
       :on-change on-toggle-all
       :checked   (not is-not-all-checked)}]
     [:label {:for "toggle-all"} "Mark all as complete"]
     [:ul.todo-list
      (map #(let [it (nth items %)]
              (rum/with-key
                (--todo-list-item
                  {:identity       (:id it)
                   :text           (:value it)
                   :checked        (:checked it)
                   :editing        (:editing it)
                   :on-remove-item on-remove-item
                   :on-toggle      (fn [identity checked] (on-toggle-item identity checked))}) (:id it)))
           items-keys)]]))

(rum/defc --footer
  [has-item-completed on-clear-completed item-size]
  [:footer.footer
   [:span.todo-count
    [:strong item-size]
    " item left"]
   (if has-item-completed [:button.clear-completed
                           {:on-click on-clear-completed}
                           "Clear completed"])])

(defn restore-app-state []
  (let [app-state (js->clj (js/JSON.parse (js/localStorage.getItem app-cache-key)))]
    (if (vector? app-state) app-state [])))

(rum/defcs --app
  < (rum/local {:items (restore-app-state) :version "0.0.1"})
    {:did-mount (fn [state]
                  (let [*state (:rum/local state)]
                    (add-watch *state :items (fn [k a o n]
                                               (let [cache-str (js/JSON.stringify (clj->js (:items n)))]
                                                 (js/console.log "changed")
                                                 (js/localStorage.setItem app-cache-key cache-str))))
                    state))
     }
    "rum component"
  [state]
  (let [*lst (:rum/local state)
        get-items #(@*lst :items)
        item-size (count (get-items))
        has-item-completed (some #(true? (:checked %)) (get-items))
        fix-item-identities #(swap! *lst assoc :items (into []
                                                            (map-indexed
                                                              (fn [k v]
                                                                (js/console.log k v)
                                                                (assoc v :id k)) (get-items))))
        wrap-input (fn [size value] {:id      size
                                     :value   value
                                     :checked false
                                     :editing false})
        map-items (fn [it-handler]
                    (swap! *lst #(assoc @*lst :items (into [] (map it-handler (get-items))))))
        on-add-todo (fn [input]
                      (if (not= input "")
                        (swap! *lst #(assoc @*lst :items (conj (:items @*lst) (wrap-input item-size input))))))
        on-toggle-all (fn [e]
                        (let [is-checked (.. e -target -checked)]
                          (map-items #(assoc % :checked is-checked))))
        on-toggle-item (fn [identity checked]
                         (do
                           (js/console.log identity)
                           (swap! *lst assoc-in [:items identity :checked] checked)))
        on-clear-completed (fn []
                             (if has-item-completed
                               (do
                                 (swap! *lst assoc :items (filterv #(not (:checked %)) (get-items)))
                                 (fix-item-identities))))
        on-remove-item (fn [identity]
                         (swap! *lst assoc :items (filterv #(not= identity (:id %)) (get-items)))
                         (fix-item-identities))
        ]
    [:div.todoapp
     (--header {:on-enter on-add-todo})
     (--content (get-items) on-toggle-all on-toggle-item on-remove-item)
     (--footer has-item-completed on-clear-completed item-size)])
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
  (rum/mount (--app) (get-container-el))
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

