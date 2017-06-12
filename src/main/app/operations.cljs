(ns app.operations
  (:require
    [untangled.client.mutations :as m :refer [defmutation]]))

(defmutation delete-person
   "Mutation: Delete the person with person-id from the list with list-id"
               [{:keys [list-id person-id]}]
   (action [{:keys [state]}]
     (let [ident-to-remove [:person/by-id person-id]
           strip-fk        (fn [old-fks]
                             (vec (filter #(not= ident-to-remove %) old-fks)))]
       (swap! state update-in [:person-list/by-id list-id :person-list/people] strip-fk)))
  (remote [env] true))
