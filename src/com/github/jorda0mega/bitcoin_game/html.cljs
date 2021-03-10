(ns com.github.jorda0mega.bitcoin-game.html
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [hiccups.runtime :as hiccups]))
            ;["rangeslider" :as slider]))

(defn PageTitle []
  [:h1.display-3
   [:img {:src "/assets/bitcoin.png" :width "10%" :height "10%"}] "itcoin Trader"])

(defn Start []
  [:div [:input {:id "startButton" :class "start" :type "button" :value "Start"}]])

(defn Reset []
  [:div [:input {:id "resetButton" :class "reset" :type "button" :value "Reset"}]])

(defn UpButton []
  [:input {:id "upButton" :class "upBtn" :type "button" :value "⬆"}])

(defn DownButton []
  [:input {:id "downButton" :class "downBtn" :type "button" :value "⬇"}])

(defn ButtonPanel []
  [:div {:id "buttonPanel" :class "btnPanel"}
   (UpButton)
   (DownButton)])

;(defn Slider []
;  [:div (slider/createSliderWithTooltip)])

(defn PriceSection [price]
  [:div.price
   [:span.label (str "$" price)]])

(hiccups/defhtml BitcoinGame
                 [{:keys [price]}]
                 (.log js/console (str "receiving price " price))
                 [:main.text-center
                  (PageTitle)
                  (PriceSection price)
                  (Start)
                  (Reset)
                  (ButtonPanel)])
