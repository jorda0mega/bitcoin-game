(ns com.github.jorda0mega.bitcoin-game.state)

(def initial-state {:price 0
                    :is-playing-game? false})

(def state (atom initial-state))