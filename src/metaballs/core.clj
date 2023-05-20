(ns metaballs.core
  "Simple metaballs simulation, trying to replicate a lava lamp.

  Performance is pretty dire, we're doing a lot more math per frame
  that Clojure is happy about, plus there's a bunch of slow reflection
  happening under the hood."
  (:gen-class)
  (:require [quil.core :as q]
            [quip.core :as qp]
            [metaballs.scenes.sim :as sim]))

(defn setup
  "The initial state of the game"
  []
  {:metaballs (sim/init-metaballs)
   :draw-boundaries? false})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:sim (sim/init)})

(def metaballs-game
  (qp/game {:title          "metaballs"
            :size           [200 200]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :sim}))

(defn -main
  [& args]
  (qp/run metaballs-game))
