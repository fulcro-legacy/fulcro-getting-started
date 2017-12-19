(ns app.intro
  (:require [fulcro.client.cards :refer [defcard-fulcro]]
            [app.ui.root :as root]
            [fulcro.client.data-fetch :as df]
            [app.rest :as rest]
            [app.api.mutations :as api]
            [fulcro.client.network :as net]))

(defcard-fulcro sample-app
  root/Root
  {}
  {:inspect-data true
   :fulcro       {
                  :networking {:remote (net/make-fulcro-network "/api" :global-error-callback (constantly nil))
                               :rest   (rest/make-rest-network)}
                  :started-callback
                              (fn [app]
                                (df/load app :current-user root/Person)
                                (df/load app :posts root/Post {:remote :rest :target [:post-list/by-id :the-one :posts]})
                                (df/load app :my-friends root/Person {:target        [:person-list/by-id :friends :person-list/people]
                                                                      :post-mutation `api/sort-friends})
                                (df/load app :my-enemies root/Person {:target [:person-list/by-id :enemies :person-list/people]}))}})
