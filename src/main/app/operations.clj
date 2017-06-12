(ns app.operations
  (:require
    [untangled.server :as server :refer [defquery-root defquery-entity defmutation]]
    [taoensso.timbre :as timbre]))

(def people-db (atom {1 {:db/id 1 :person/name "Server Joe" :person/age 55 :person/relation :friend}
                      2 {:db/id 2 :person/name "Server Sally" :person/age 22 :person/relation :friend}
                      3 {:db/id 3 :person/name "Server Judy" :person/age 76 :person/relation :enemy}
                      4 {:db/id 4 :person/name "Server Sam" :person/age 32 :person/relation :friend}}))

(defmutation delete-person
  "Server Mutation: Handles deleting a person on the server"
  [{:keys [list-id person-id]}]
  (action [{:keys [state]}]
    (timbre/info "Server Deleting " person-id " from " list-id)
    (swap! people-db dissoc person-id)))

(defn get-people [kind keys]
  (->> @people-db
    vals
    (filter #(= kind (:person/relation %)))
    vec))

(defquery-root :my-friends
  "Queries for friends and returns them to the client"
  (value [{:keys [query]} params]
    (get-people :friend query)))

(defquery-root :my-enemies
  "Queries for friends and returns them to the client"
  (value [{:keys [query]} params]
    (get-people :enemy query)))
