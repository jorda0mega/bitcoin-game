(ns com.github.jorda0mega.bitcoin-game.html
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccups]))

(defn PageTitle []
  [:h1.display-3
   [:img {:src "/assets/bitcoin.png" :width "10%" :height "10%"}] "itcoin Trader"])

(defn Start []
  [:div [:input {:id "startButton" :class "start" :type "button" :value "Start"}]])

(defn Reset []
  [:div [:input {:id "resetButton" :class "reset" :type "button" :value "Reset"}]])

(defn PriceSlider []
  [:div.rangeSlider {:id "divRangeSlider"} [:input {:id "inputRangeSlider" :type "range" :min "0" :max "1000" :step "100" :value= "500"}]])

(defn UpButton []
  [:div.divUpButton [:input {:id "upButton" :class "upBtn" :type "button" :value "⬆"}]])

(defn DownButton []
  [:div.divDownButton [:input {:id "downButton" :class "downBtn" :type "button" :value "⬇"}]])

(defn ButtonPanel []
  [:div.divButtonPanel {:id "buttonPanel" :class "btnPanel"}
   (DownButton)
   (UpButton)])

(defn GameOver []
  [:div.gameOver {:id "divGameOver"}
   [:img.imgGameOver {:src "assets/broke.jpeg" :width "70%" :height "70%"}]])

(defn PriceSection [price]
  [:div.price {:id "divPrice"}
   [:span.label (str "$" price)]])

(defn Profits [profit]
  [:div.profit {:id "divProfit"}
   [:span.label (str "Profits: $" profit)]])

(hiccups/defhtml BitcoinGame
                 [{:keys [price profit]}]
                 (.log js/console (str "receiving price " price))
                 [:main.text-center
                  (PageTitle)
                  (Profits profit)
                  (PriceSection price)
                  (Start)
                  (GameOver)
                  (Reset)
                  ;(PriceSlider)
                  (ButtonPanel)])
