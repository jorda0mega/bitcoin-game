(ns bitcoin-game.interaction
  (:require
    [bitcoin-game.state :refer [initial-state state]]
    [bitcoin-game.html :as html]
    [goog.dom :as gdom]
    [taoensso.timbre :as timbre]
    [goog.functions :as gfunctions]
    [oops.core :refer [ocall oget oset!]]
    [ajax.core :refer [GET]]))

(defn set-app-html!
  "Sets the application html"
  [html-str]
  (let [el (gdom/getElement "appContainer")]
    (oset! el "innerHTML" html-str)))

(defn render-ui! [_ _kwd _prev-state new-state]
  (timbre/trace "getting new state")
  (timbre/trace new-state)
  (set-app-html! (html/BitcoinGame new-state)))

(defn fetch-bitcoin-price-success [response]
  (let [current-state @state
        new-price (:price_usd (first response))]
    (when (:is-playing-game? current-state)
      (swap! state (fn [state]
                     (-> state
                         (assoc :price new-price)))))))

(defn fetch-bitcoin-price-error [{:keys [status status-text]}]
  (.log js/console (str "bad request to bitcoin charts: " status " " status-text)))

(defn fetch-bitcoin-price
  "ajax request to fetch bitcoin current price"
  []
  (GET "https://api.coinlore.net/api/ticker/?id=90" {:handler         fetch-bitcoin-price-success
                                                     :error-handler   fetch-bitcoin-price-error
                                                     :response-format :json
                                                     :keywords?       true}))

(defn start-game []
  (swap! state (fn [state]
                 (assoc state :is-playing-game? true)))
  (fetch-bitcoin-price))

(defn reset-game []
  (reset! state initial-state))

(defn handle-game-started []
  (oset! (gdom/getElement "buttonPanel") "style.display" "inline-block")
  (oset! (gdom/getElement "startButton") "style.display" "none")
  (oset! (gdom/getElement "resetButton") "style.display" "inline-block"))

(defn handle-game-state [_ _kwd _prev-state new-state]
  (when-let [is-playing-game? (:is-playing-game? new-state)]
    (if is-playing-game?
      (handle-game-started)
      (oset! (gdom/getElement "buttonPanel") "style.display" "none"))))

(defn click-app-container [js-evt]
  (let [target-el (oget js-evt "target")
        start? (ocall (oget target-el "classList") "contains" "start")
        reset? (ocall (oget target-el "classList") "contains" "reset")]
    (cond
      start? (start-game)
      reset? (reset-game))))

;; ---------------------------------------------
;; Public API

(defn trigger-render! []
  (swap! state identity))

(def init-dom-events!
  (gfunctions/once
    (fn []
      (ocall (gdom/getElement "appContainer") "addEventListener" "click" click-app-container))))

(def init-watchers!
  (gfunctions/once
    (fn []
      (add-watch state :render-ui render-ui!)
      (add-watch state :handle-game-state handle-game-state))))