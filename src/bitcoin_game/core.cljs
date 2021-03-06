(ns bitcoin-game.core
  (:require
    [bitcoin-game.interaction :refer [init-dom-events! init-watchers! trigger-render!]]
    [goog.functions :as gfunctions]
    [oops.core :refer [ocall]]))

(def init!
  (gfunctions/once
    (fn []
      (println "Initializing Bitcoin Game!")
      (.log js/console "Starting Bitcoin Game!")
      (init-dom-events!)
      (init-watchers!)
      (trigger-render!))))

(ocall js/window "addEventListener" "load" init!)