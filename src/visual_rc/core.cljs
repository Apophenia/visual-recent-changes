(ns visual-rc.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan put! timeout]]
            [clojure.string :as string])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn log [m]
(.log js/console m))

(def websocket* (atom nil))
(def connect-string "ws://")

(log "opening connection with websocket")
(reset! websocket* (js/WebSocket. connect-string))

(def inputChannel (chan 10))

(doall
(map #(aset @websocket* (first %)(second %))
      [["onopen" (fn [] (log "open!"))]
       ["onclose" (fn [] (log "closed!"))]
       ["onerror" (fn [e] (log (str "oh no!:" e)))]
       ["onmessage" (fn [m] (go (>! inputChannel m)))]]))


(go 
 (loop [x 10]
   (when (pos? x)
     (log (<! inputChannel))
     (recur (dec x)))))

