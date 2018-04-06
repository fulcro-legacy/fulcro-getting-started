(ns app.server
  (:require [fulcro.easy-server :as easy]
            [fulcro.server :as server]
            app.operations))

(defn make-system [config-path]
  (easy/make-fulcro-server
    :config-path config-path
    :parser (server/fulcro-parser)))

