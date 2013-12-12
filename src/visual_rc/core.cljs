(ns visual-rc.core
  (:require [ cljs.core.async :as async
             :refer [<! >! chan put! timeout]]
             [clojure.string :as string]
             [visual-rc.config :as config])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn log [m]
  (.log js/console (str m)))

(def config config/config)

(def input-channel (chan 10))

(defn make-socket [lang address]
  (let [socket (js/WebSocket. address)]
    (doall
    (map #(aset socket (first %)(second %))
         [["onopen" (fn [] (log "open!"))]
          ["onclose" (fn [] (log "closed!"))]
          ["onerror" (fn [e] (log (str "oh no!:" e)))]
          ["onmessage" (fn [m] (go (>! input-channel [lang m])))]]))
    socket))

(def websockets (into {} (for [[lang address] config]
                           [lang (make-socket lang address)])))

(def canvas (.getElementById js/document "canvas"))

(def context
      (.getContext canvas "2d"))

(defn set-random-bg [context]
  (let [w (.-width canvas)
        h (.-height canvas)
        [r g b] (take 3 (repeatedly #(rand-int 255)))
        rgbstr (str "rgb(" r "," g "," b ")")]
    (set! (.-fillStyle context) rgbstr)
    (.fillRect context 0 0 w h)))

(defn draw-circle [context lang]
  (let [color (case lang
             :english "#314170" ;dark blue
             :german  "#ED8931" ;orange
             :russian "#99B3FB" ;light blue
             :japanese "#DF4527" ;red
             :french "#FFFFFF" ;white
             :italian "#53C549" ;green
             :polish "#9972A8" ;purple
             :spanish "#E8CD3A" ;yellow
             "000000") ;oops
        radius 10
        x (+ (rand-int (- (.-width canvas) (* 2 radius))) radius)
        y (+ (rand-int (- (.-height canvas) (* 2 radius))) radius)
        ]
    (set! (.-fillStyle context) color)
    (.beginPath context)
    (.arc context x y radius 0 (* 2 (.-PI js/Math) 10) false)
    (.fill context)))

(set! (.-fillStyle context) "#D9D9D9")
(.fillRect context 0 0 (.-width canvas) (.-height canvas))

(go
 (loop [x 10000]
   (when (pos? x)
     (let [color (first (<! input-channel))]
       (log color)
           (draw-circle context color))
     (recur (dec x)))))
