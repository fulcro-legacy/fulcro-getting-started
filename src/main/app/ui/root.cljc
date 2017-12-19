(ns app.ui.root
  (:require
    translations.es
    [fulcro.client.dom :as dom]
    [app.api.mutations :as api]
    [fulcro.client.data-fetch :as df]
    [fulcro.client.primitives :as prim :refer [defsc]]))

(defsc Post [this {:keys [post/title post/body]}]           ; <1>
  {:ident [:posts/by-id :db/id]
   :query [:db/id :post/user-id :post/body :post/title]}
  (dom/div nil
    (dom/h4 nil title)
    (dom/p nil body)))

(def ui-post (prim/factory Post {:keyfn :db/id}))

(defsc Posts [this {:keys [posts]}]                         ; <2>
  {:initial-state {:posts []}
   :ident         (fn [] [:post-list/by-id :the-one])
   :query         [{:posts (prim/get-query Post)}]}
  (dom/ul nil
    (map ui-post posts)))

(def ui-posts (prim/factory Posts))

(defsc Person [this {:keys [db/id person/name person/age]} {:keys [onDelete]}]
  {:query         [:db/id :person/name :person/age]
   :ident         [:person/by-id :db/id]
   :initial-state (fn [{:keys [id name age]}] {:db/id id :person/name name :person/age age})}
  (dom/li nil
    (dom/h5 nil name (str "(age: " age ")")
      (dom/button #js {:onClick #(onDelete id)} "X")
      (dom/button #js {:onClick #(df/refresh! this)} "Refresh"))))

(def ui-person (prim/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:keys [db/id person-list/label person-list/people]}]
  {:query [:db/id :person-list/label {:person-list/people (prim/get-query Person)}]
   :ident [:person-list/by-id :db/id]
   :initial-state
          (fn [{:keys [id label]}]
            {:db/id              id
             :person-list/label  label
             :person-list/people []})}
  (let [delete-person (fn [person-id] (prim/transact! this `[(api/delete-person {:list-id ~id :person-id ~person-id})]))]
    (dom/div nil
      (dom/h4 nil label)
      (dom/ul nil
        (map (fn [p] (ui-person (prim/computed p {:onDelete delete-person}))) people)))))

(def ui-person-list (prim/factory PersonList))

(defsc Root [this {:keys [ui/react-key blog-posts current-user friends enemies]}]
  {:query         [:ui/react-key
                   :ui/person-id
                   {:current-user (prim/get-query Person)}
                   {:blog-posts (prim/get-query Posts)}     ; <3>
                   {:friends (prim/get-query PersonList)}
                   {:enemies (prim/get-query PersonList)}]
   :initial-state (fn [params] {:blog-posts (prim/get-initial-state Posts {}) ; <4>
                                :friends    (prim/get-initial-state PersonList {:id :friends :label "Friends"})
                                :enemies    (prim/get-initial-state PersonList {:id :enemies :label "Enemies"})})}
  (dom/div #js {:key react-key}
    (dom/h4 nil (str "Current User: " (:person/name current-user)))
    (dom/button #js {:onClick (fn [] (df/load this [:person/by-id 3] Person))} "Refresh User with ID 3")
    (ui-person-list friends)
    (ui-person-list enemies)
    (dom/h4 nil "Blog Posts")                               ; <6>
    (ui-posts blog-posts)))
