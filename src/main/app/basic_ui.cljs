(ns app.basic-ui
  (:require [fulcro.client.core :as uc]
            [om.dom :as dom]
            [app.operations :as ops]
            [om.next :as om :refer [defui]]
            [app.rest :as rest]
            [fulcro.client.data-fetch :as df]
            [fulcro.client.mutations :as m]
            [fulcro.client.network :as net]))

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

(defui Post
  static om/Ident
  (ident [this props] [:posts/by-id (:db/id props)])
  static om/IQuery
  (query [this] [:db/id :post/user-id :post/body :post/title])
  Object
  (render [this]
    (let [{:keys [post/title post/body]} (om/props this)]
      (dom/div nil
        (dom/h4 nil title)
        (dom/p nil body)))))

(def ui-post (om/factory Post {:keyfn :db/id}))

(defui Posts
  static uc/InitialAppState
  (initial-state [c params] {:posts []})
  static om/Ident
  (ident [this props] [:post-list/by-id :the-one])
  static om/IQuery
  (query [this] [{:posts (om/get-query Post)}])
  Object
  (render [this]
    (let [{:keys [posts]} (om/props this)]
      (dom/ul nil
        (map ui-post posts)))))

(def ui-posts (om/factory Posts))

(defui ^:once Root
  static om/IQuery
  (query [this] [:ui/react-key
                 :ui/person-id
                 {:current-user (om/get-query Person)}
                 {:blog-posts (om/get-query Posts)}
                 {:friends (om/get-query PersonList)}
                 {:enemies (om/get-query PersonList)}])
  static
  uc/InitialAppState
  (initial-state [c params] {:blog-posts (uc/get-initial-state Posts {})
                             :friends    (uc/get-initial-state PersonList {:id :friends :label "Friends"})
                             :enemies    (uc/get-initial-state PersonList {:id :enemies :label "Enemies"})})
  Object
  (render [this]
    ; NOTE: the data now comes in through props!!!
    (let [{:keys [ui/react-key blog-posts current-user friends enemies]} (om/props this)]
      (dom/div #js {:key react-key}
        (dom/h4 nil (str "Current User: " (:person/name current-user)))
        (dom/button #js {:onClick (fn [] (df/load this [:person/by-id 3] Person))} "Refresh User with ID 3")
        (ui-person-list friends)
        (ui-person-list enemies)
        (dom/h4 nil "Blog Posts")
        (ui-posts blog-posts)))))

(defonce app-1 (atom (uc/new-fulcro-client
                       :networking {:remote (net/make-fulcro-network "/api" :global-error-callback (constantly nil))
                                    :rest   (rest/make-rest-network)}
                       :started-callback (fn [app]
                                           (df/load app :posts Post {:remote :rest :target [:post-list/by-id :the-one :posts]})
                                           (df/load app :current-user Person)
                                           (df/load app :my-friends Person {:target        [:person-list/by-id :friends :person-list/people]
                                                                            :post-mutation `ops/sort-friends})
                                           (df/load app :my-enemies Person {:target [:person-list/by-id :enemies :person-list/people]})))))
