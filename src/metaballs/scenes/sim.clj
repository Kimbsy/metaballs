(ns metaballs.scenes.sim
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [metaballs.common :as common]))

;; Satisfying threshold value found by experimentation
(def threshold 0.9)

(defn mb-fn
  "Generate a metaball falloff function based on it's current position
  and size"
  [{[x0 y0] :pos :keys [size] :as mb}]
  ;; returned function expects a pixel position
  (fn [[x y]]
    (/ size
       (+ (Math/pow (- x x0) 2)
          (Math/pow (- y y0) 2)))))

(defn metaball
  "Create a metaball, `size` is a non-linear, abstract unit of size"
  [pos vel size]
  (let [mb {:pos pos
            :vel vel
            :size size}]
    (assoc mb :fn (mb-fn mb))))

(defn init-metaballs
  "Describe the initial metaballs"
  []
  [(metaball [50 50] [1 1] 200)
   (metaball [150 100] [-1 0] 50)
   (metaball [75 150] [1 -1] 600)])

(defn update-metaball
  "Update a metaballs positions based on its veloctiy, then update its
  falloff function based on its new position"
  [{:keys [pos vel] :as metaball}]
  (as-> metaball mb
    (assoc mb :pos (map + pos vel))
    (assoc mb :fn (mb-fn mb))))

(defn filled?
  "A pixel should be filled if the sum of all metaball falloff functions
  for that position is greater than the threshold value"
  [{:keys [metaballs]} pos]
  (<= threshold
      (reduce + (map (fn [mb] ((:fn mb) pos)) metaballs))))

(defn draw-sim
  "Called each frame, draws the current scene to the screen"
  [state]
  ;; get the pixel Array so we can force new values with `aset-int`
  (let [pxs (q/pixels)]
    (doall
     (map (fn [[x y :as pos]]
            ;; create an integer color based on whether the pixel
            ;; should be filled
            (let [c (apply q/color (if (filled? state pos)
                                     common/light-green
                                     common/dark-green))]

              ;; jam it in the pixel array
              (aset-int pxs (+ (* y (q/width)) x) c)))

          ;; generate all positions on screen
          (for [x (range (q/width))
                y (range (q/height))]
            [x y])))

    ;; update the graphics object with the new pixel values
    (q/update-pixels)))

(defn update-sim
  "Called each frame, updates the current scene"
  [state]
  (if (< 180 (-> state :metaballs first :pos first))
    (assoc state
           :metaballs
           (init-metaballs))
    (update state
            :metaballs
            (partial map update-metaball))))

(defn init
  "Initialise this scene"
  []
  {:draw-fn draw-sim
   :update-fn update-sim
   :mouse-pressed-fns [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})
clojure.core/abs
