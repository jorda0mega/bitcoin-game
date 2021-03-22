(ns com.github.jorda0mega.bitcoin-game.interaction
  (:require
    [com.github.jorda0mega.bitcoin-game.state :refer [initial-state state]]
    [com.github.jorda0mega.bitcoin-game.html :as html]
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
  (set-app-html! (html/BitcoinGame new-state)))

(def randomize-price? true)

(defn parse-bitcoin-price
  "parses the current bitcoin price from the response"
  [response]
  (let [current-price (:price @state)
        btcusd-pair (filter (fn [pricefeed]
                              (= (:pair pricefeed) "BTCUSD")) response)
        new-price (:price (first btcusd-pair))]
    (if (and randomize-price? (= current-price new-price))
      (* (rand 2) current-price)
      new-price)))

(defn start-game-success [response]
  (swap! state (fn [current-state]
                 (let [new-price (parse-bitcoin-price response)]
                   (if-not (:is-playing-game? current-state)
                     current-state
                     (assoc current-state :price new-price))))))


(defn profit-from-vote
  "determines whether the vote was correct/wrong and adds/removes profits accordingly"
  [vote-result bid]
  (let [current-profit (:profit @state)]
    (if vote-result
      (+ current-profit bid)
      (- current-profit bid))))

;(defn fetch-bitcoin-price-success! [response]
;  (let [new-price (:price_usd (first response))]
;    (timbre/log "new price of bitcoin: " new-price)
;    (swap! state (fn [current-state]
;                   (let [current-price (js/parseFloat (:price current-state))]
;                     (if (= current-price new-price)
;                       (assoc state :price (* (rand 2) current-price))
;                       (assoc state :price new-price)))))
;    new-price))

(defn vote-up-success
  "handle voting up "
  [response]
  (timbre/info "calling vote up")
  (swap! state (fn [current-state]
                 (let [new-price (parse-bitcoin-price response)]
                   (assoc current-state
                     :price new-price
                     :profit (profit-from-vote (>= new-price (:price current-state)) 100))))))

(defn vote-down-success
  "handle voting up "
  [response]
  (timbre/info "calling vote down")
  (swap! state (fn [current-state]
                 (let [new-price (parse-bitcoin-price response)]
                   (assoc current-state
                     :price new-price
                     :profit (profit-from-vote (<= new-price (:price current-state)) 100))))))


(defn vote-success
  "handle voting up "
  [direction response]
  (timbre/info "calling vote down")
  (swap! state (fn [current-state]
                 (let [new-price (parse-bitcoin-price response)
                       operator (if (= direction :up) >= <=)]
                   (assoc current-state
                     :price new-price
                     :profit (profit-from-vote (operator new-price (:price current-state)) 100))))))

(defn fetch-bitcoin-price-error [{:keys [status status-text]}]
  (timbre/error "bad request to bitcoin price: " status " " status-text))

(defn fetch-bitcoin-price
  "ajax request to fetch bitcoin current price"
  [success-fn]
  (GET "https://api.gemini.com/v1/pricefeed" {:handler         success-fn
                                              :error-handler   fetch-bitcoin-price-error
                                              :response-format :json
                                              :keywords?       true}))

(defn start-game []
  (swap! state (fn [state]
                 (assoc state :is-playing-game? true)))
  (fetch-bitcoin-price start-game-success))

(defn reset-game []
  (reset! state initial-state))

(defn vote-up
  "voting that the bitcoin price will go up"
  []
  (timbre/info "I bet the bitcoin price is going up")
  (fetch-bitcoin-price (partial vote-success :up))
  (fetch-bitcoin-price #(vote-success:up %)))


(defn vote-down
  "voting that the bitcoin price will go down"
  []
  (timbre/info "I bet the bitcoin price is going down")
  (fetch-bitcoin-price (partial vote-success :down)))

(defn handle-game-started []
  (oset! (gdom/getElement "startButton") "style.display" "none")
  (oset! (gdom/getElement "divPrice") "style.display" "inline-block")
  (oset! (gdom/getElement "divProfit") "style.display" "block")
  (oset! (gdom/getElement "buttonPanel") "style.display" "inline-block")
  (oset! (gdom/getElement "resetButton") "style.display" "inline-block"))
;(oset! (gdom/getElement "divRangeSlider") "style.display" "block"))

(defn game-over? []
  (when (< (:profit @state) 0)
    (oset! (gdom/getElement "startButton") "style.display" "none")
    (oset! (gdom/getElement "divPrice") "style.display" "none")
    (oset! (gdom/getElement "divProfit") "style.display" "none")
    (oset! (gdom/getElement "buttonPanel") "style.display" "none")
    (oset! (gdom/getElement "divGameOver") "style.display" "inline-block")))

(defn handle-game-state [_ _kwd _prev-state new-state]
  (let [is-playing-game? (:is-playing-game? new-state)]
    (when is-playing-game?
      (handle-game-started)
      (game-over?))))

(defn click-app-container [js-evt]
  (let [target-el (oget js-evt "target")
        start? (ocall target-el "classList.contains" "start")
        reset? (ocall target-el "classList.contains" "reset")
        going-up? (ocall target-el "classList.contains" "upBtn")
        going-down? (ocall target-el "classList.contains" "downBtn")]
    (cond
      start? (start-game)
      reset? (reset-game)
      going-up? (vote-up)
      going-down? (vote-down)
      :else nil)))

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