(ns cljs.user
  (:require
    [app.basic-ui :refer [app-1 Root]]
    [untangled.client.core :as uc]))

(defn refresh [] (swap! app-1 uc/mount Root "app-1"))

(refresh)
