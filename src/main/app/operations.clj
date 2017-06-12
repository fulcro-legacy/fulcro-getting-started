(ns app.operations
  (:require
    [untangled.server :as server :refer [defquery-root defquery-entity defmutation]]
    [taoensso.timbre :as timbre]))


(defmutation delete-person
  "Server Mutation: Handles deleting a person on the server"
  [{:keys [list-id person-id]}]
  (action [env]
    (timbre/info "Server Deleting " person-id " from " list-id)))

(defquery-root :my-friends
  "Queries for friends and returns them to the client"
  (value [env params]
    [{:db/id 1 :person/name "Server Sam" :person/age 44}]))

(defquery-root :my-enemies
  "Queries for friends and returns them to the client"
  (value [env params]
    [{:db/id 2 :person/name "Server Larry" :person/age 99}]))
