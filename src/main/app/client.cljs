(ns app.client
  (:require [fulcro.client :as fc]
            [fulcro.client.data-fetch :as df]
            [app.ui.root :as root]))

(defonce app (atom (fc/new-fulcro-client
                     :started-callback
                     (fn [app]
                       (println :LOAD)
                       (df/load app :current-user root/Person)))))
