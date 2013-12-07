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

(defn make-socket [name address]
  (let [socket (js/WebSocket. address)]
    (doall
    (map #(aset socket (first %)(second %))
         [["onopen" (fn [] (log "open!"))]
          ["onclose" (fn [] (log "closed!"))]
          ["onerror" (fn [e] (log (str "oh no!:" e)))]
          ["onmessage" (fn [m] (go (>! input-channel [name m])))]]))
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

(go
 (loop [x 100]
   (when (pos? x)
     (log (<! input-channel))
     (set-random-bg context)
     (recur (dec x)))))
