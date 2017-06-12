(ns app.basic-ui
  (:require [untangled.client.core :as uc]
            [om.dom :as dom]
            [app.operations :as ops]
            [om.next :as om :refer [defui]]
            [untangled.client.data-fetch :as df]
            [untangled.client.mutations :as m]))

(defui ^:once Person
  static om/Ident
  (ident [this props] [:person/by-id (:db/id props)])
  static om/IQuery
  (query [this] [:db/id :person/name :person/age])
  static uc/InitialAppState
  (initial-state [comp-class {:keys [id name age] :as params}] {:db/id id :person/name name :person/age age})
  Object
  (render [this]
    (let [{:keys [db/id person/name person/age]} (om/props this)
          onDelete (om/get-computed this :onDelete)]
      (dom/li nil
        (dom/h5 nil name (str "(age: " age ")")
          (dom/button #js {:onClick #(df/refresh! this)} "Refresh")
          (dom/button #js {:onClick #(onDelete id)} "X"))))))

(def ui-person (om/factory Person {:keyfn :person/name}))

(defui ^:once PersonList
  static om/Ident
  (ident [this props] [:person-list/by-id (:db/id props)])
  static om/IQuery
  (query [this] [:db/id :person-list/label {:person-list/people (om/get-query Person)}])
  static uc/InitialAppState
  (initial-state [comp-class {:keys [id label]}]
    {:db/id              id
     :person-list/label  label
     :person-list/people []})
  Object
  (render [this]
    (let [{:keys [db/id person-list/label person-list/people]} (om/props this)
          delete-person (fn [person-id]
                          (js/console.log label "asked to delete" name)
                          (om/transact! this `[(ops/delete-person {:list-id ~id :person-id ~person-id})]))]
      (dom/div nil
        (dom/h4 nil label)
        (dom/ul nil
          (map (fn [person] (ui-person (om/computed person {:onDelete delete-person}))) people))))))

(def ui-person-list (om/factory PersonList))

(defui ^:once Root
  static om/IQuery
  (query [this] [:ui/react-key
                 :ui/person-id
                 {:friends (om/get-query PersonList)}
                 {:enemies (om/get-query PersonList)}])
  static
  uc/InitialAppState
  (initial-state [c params] {:friends (uc/get-initial-state PersonList {:id :friends :label "Friends"})
                             :enemies (uc/get-initial-state PersonList {:id :enemies :label "Enemies"})})
  Object
  (render [this]
    ; NOTE: the data now comes in through props!!!
    (let [{:keys [ui/react-key friends enemies]} (om/props this)]
      (dom/div #js {:key react-key}
        (dom/button #js {:onClick #(df/load this [:person/by-id 3] Person {})} "Refresh Person with ID 3")
        (ui-person-list friends)
        (ui-person-list enemies)))))

(defonce app-1 (atom (uc/new-untangled-client
                       :started-callback (fn [app]
                                           (df/load app :my-friends Person {:target [:person-list/by-id :friends :person-list/people]})
                                           (df/load app :my-enemies Person {:target [:person-list/by-id :enemies :person-list/people]})))))
