(ns metaballs.scenes.sim
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.scene :as qpscene]
            [quip.utils :as qpu]
            [metaballs.common :as common]))

;; Satisfying threshold value found by experimentation
(def threshold 0.9)

(def w (memoize q/width))
(def h (memoize q/height))

(defn falloff-fn
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
    (assoc mb :fn (falloff-fn mb))))

(defn init-metaballs
  "Describe the initial metaballs"
  []
  [(metaball [50 50] [1 1] 200)
   (metaball [150 100] [-1 0] 50)
   (metaball [75 150] [1 -1] 600)
   (metaball [130 180] [0 -1] 100)])

(defn random-metaballs
  []
  (->> (fn []
         (metaball [(rand-int (w)) (rand-int (h))]
                   [(rand-nth [-1 0 1]) (rand-nth [-1 0 1])]
                   (rand-int 500)))
       repeatedly
       (take 4)))

(defn update-metaball
  "Update a metaballs positions based on its veloctiy, then update its
  falloff function based on its new position"
  [{:keys [pos vel] :as metaball}]
  (as-> metaball mb
    (assoc mb :pos (map + pos vel))
    (assoc mb :fn (falloff-fn mb))))

(defn pos-val
  "Get the sum of all metaball falloff functions for a position"
  [{:keys [metaballs]} pos]
  (transduce (map (fn [mb] ((:fn mb) pos))) + metaballs))

(defn boundary?
  "A pixel should be drawn as a boundary if the sum of all metaball
  falloff functions is very close to the threshold value"
  [val]
  (< -0.1 (- threshold val) 0.1))

(defn filled?
  "A pixel should be filled if the sum of all metaball falloff functions
  for that position is greater than the threshold value"
  [val]
  (<= threshold val))

(def positions
  (memoize
   (fn []
     (for [x (range (w))
           y (range (h))]
       [x y (+ (* y (w)) x)]))))

(def pink (memoize (fn [] (apply q/color common/pink))))
(def light-green (memoize (fn [] (apply q/color common/light-green))))
(def dark-green (memoize (fn [] (apply q/color common/dark-green))))

(defn draw-sim
  "Called each frame, draws the current scene to the screen"
  [{:keys [draw-boundaries?] :as state}]
  ;; get the pixel Array so we can force new values with `aset-int`
  (let [pxs (q/pixels)]
    (doall
     (map (fn [[x y i]]
            ;; create an integer color based on whether the pixel
            ;; should be filled
            (let [val (pos-val state [x y])
                  c (cond
                      (and draw-boundaries? (boundary? val)) (pink)
                      (filled? val) (light-green)
                      :else (dark-green))]

              ;; jam it in the pixel array
              (aset-int pxs (+ (* y (w)) x) c)))

          ;; generate all positions on screen
          (-> state :scenes :sim :positions)))

    ;; update the graphics object with the new pixel values
    (q/update-pixels)))

(defn update-sim
  "Called each frame, updates the current scene"
  [state]
  (update state
          :metaballs
          (partial map update-metaball)))

(defn toggle-boundaries
  "Toggle whether to draw metaball boundaries"
  [state _e]
  (update state :draw-boundaries? not))

(defn key-pressed
  [state e]
  (case (:key e)
    :r (assoc state :metaballs (random-metaballs))
    :space (assoc state :metaballs (init-metaballs))
    (toggle-boundaries state e)))

(defn init
  "Initialise this scene"
  []
  {:draw-fn draw-sim
   :update-fn update-sim
   :mouse-pressed-fns [toggle-boundaries]
   :key-pressed-fns [key-pressed]
   :positions (for [x (range (w))
                    y (range (h))]
                [x y (+ (* y (w)) x)])})
