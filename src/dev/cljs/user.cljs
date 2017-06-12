(ns cljs.user
  (:require
    [app.basic-ui :refer [app-1 Root]]
    [untangled.client.core :as uc]))

(defn refresh [] (swap! app-1 uc/mount Root "app-1"))

(refresh)

(defn dump
  [& keys]
  (let [state-map        @(om.next/app-state (-> app-1 deref :reconciler))
        data-of-interest (if (seq keys)
                           (get-in state-map keys)
                           state-map)]
    data-of-interest))
