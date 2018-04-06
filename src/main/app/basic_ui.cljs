(ns app.basic-ui
  (:require [fulcro.client :as fc]
            [app.operations :as ops]
            [app.rest :as rest]
            [fulcro.client.data-fetch :as df]
            [fulcro.client.primitives :as prim :refer [defsc defui]]
            [fulcro.client.dom :as dom]
            [fulcro.client.mutations :as m]
            [fulcro.client.network :as net]))

(defsc Person [this {:keys [db/id person/name person/age]} {:keys [onDelete]}]
  {
   :ident         [:person/by-id :db/id]
   :query         [:db/id :person/name :person/age]
   :initial-state (fn [{:keys [id name age] :as params}] {:db/id id :person/name name :person/age age})}
  (dom/li
    (dom/h5 (str name " (age: " age ")")
      (dom/button {:onClick #(df/refresh! this)} "Refresh")
      (dom/button {:onClick #(onDelete id)} "X"))))

(def ui-person (prim/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:keys [db/id person-list/label person-list/people]}]
  {:ident         [:person-list/by-id :db/id]
   :query         [:db/id :person-list/label {:person-list/people (prim/get-query Person)}]
   :initial-state (fn [{:keys [id label]}] {:db/id              id
                                            :person-list/label  label
                                            :person-list/people []})}
  (let [delete-person (fn [person-id]
                        (js/console.log label "asked to delete" name)
                        (prim/transact! this `[(ops/delete-person {:list-id ~id :person-id ~person-id})]))]
    (dom/div
      (dom/h4 label)
      (dom/ul
        (map (fn [person] (ui-person (prim/computed person {:onDelete delete-person}))) people)))))

(def ui-person-list (prim/factory PersonList))

(defsc Post [this {:keys [post/title post/body]}]
  {:ident [:posts/by-id :db/id]
   :query [:db/id :post/user-id :post/body :post/title]}
  (dom/div
    (dom/h4 title)
    (dom/p body)))

(def ui-post (prim/factory Post {:keyfn :db/id}))

(defsc Posts [this {:keys [posts]}]
  {:initial-state (fn [params] {:posts []})
   :ident         (fn [] [:post-list/by-id :the-one])
   :query         [{:posts (prim/get-query Post)}]}
  (dom/ul
    (map ui-post posts)))

(def ui-posts (prim/factory Posts))

(defsc Root [this {:keys [blog-posts current-user friends enemies]}]
  {:query         [:ui/person-id
                   {:current-user (prim/get-query Person)}
                   {:blog-posts (prim/get-query Posts)}
                   {:friends (prim/get-query PersonList)}
                   {:enemies (prim/get-query PersonList)}]
   :initial-state (fn [params] {:blog-posts (prim/get-initial-state Posts {})
                                :friends    (prim/get-initial-state PersonList {:id :friends :label "Friends"})
                                :enemies    (prim/get-initial-state PersonList {:id :enemies :label "Enemies"})})}
  (dom/div
    (dom/h4 (str "Current User: " (:person/name current-user)))
    (dom/button {:onClick (fn [] (df/load this [:person/by-id 3] Person))} "Refresh User with ID 3")
    (ui-person-list friends)
    (ui-person-list enemies)
    (dom/h4 "Blog Posts")
    (ui-posts blog-posts)))

(defonce app-1 (atom (fc/new-fulcro-client
                       :networking {:remote (net/make-fulcro-network "/api" :global-error-callback (constantly nil))
                                    :rest   (rest/make-rest-network)}
                       :started-callback (fn [app]
                                           (df/load app :posts Post {:remote :rest :target [:post-list/by-id :the-one :posts]})
                                           (df/load app :current-user Person)
                                           (df/load app :my-friends Person {:target        [:person-list/by-id :friends :person-list/people]
                                                                            :post-mutation `ops/sort-friends})
                                           (df/load app :my-enemies Person {:target [:person-list/by-id :enemies :person-list/people]})))))
