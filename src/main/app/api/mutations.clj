(ns app.api.mutations
  (:require
    [taoensso.timbre :as timbre]
    [app.api.read :refer [people-db]]
    [fulcro.server :refer [defmutation]]))

;; Place your server mutations here
(defmutation delete-person
  "Server Mutation: Handles deleting a person on the server"
  [{:keys [person-id]}]
  (action [{:keys [state]}]
    (timbre/info "Server deleting person" person-id)
    (swap! people-db dissoc person-id)))

