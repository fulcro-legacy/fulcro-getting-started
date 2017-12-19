(ns app.rest
  (:refer-clojure :exclude [send])
  (:require [fulcro.client.logging :as log]
            [fulcro.client.network :as net]
            [cognitect.transit :as ct]
            [goog.events :as events]
            [fulcro.transit :as t]
            [clojure.string :as str]
            [clojure.set :as set]
            [fulcro.client.primitives :as prim])
  (:import [goog.net XhrIo EventType]))

(defn make-xhrio [] (XhrIo.))

(defrecord Network [url request-transform global-error-callback complete-app transit-handlers]
  net/NetworkBehavior
  (serialize-requests? [this] true)
  net/IXhrIOCallbacks
  (response-ok [this xhr-io valid-data-callback]
    ;; Implies:  everything went well and we have a good response
    ;; (i.e., got a 200).
    (try
      (let [read-handlers (:read transit-handlers)
            ; STEP 3: Convert the JSON response into a proper tree structure to match the query
            response      (.getResponseJson xhr-io)
            edn           (js->clj response) ; convert it to clojure
            ; Rename the keys from strings to the desired UI keywords
            posts         (mapv #(set/rename-keys % {"id"     :db/id
                                                     "title"  :post/title
                                                     "userId" :post/user-id
                                                     "body"   :post/body})
                            edn)
            ; IMPORTANT: structure of the final data we send to the callback must match the nesting structure of the query
            ; [{:posts [...]}] or it won't merge correctly:
            fixed-response      {:posts posts}]
        (js/console.log :converted-response fixed-response)
        ; STEP 4; Send the fixed up response back to the client DB
        (when (and response valid-data-callback) (valid-data-callback fixed-response)))
      (finally (.dispose xhr-io))))
  (response-error [this xhr-io error-callback]
    ;; Implies:  request was sent.
    ;; *Always* called if completed (even in the face of network errors).
    ;; Used to detect errors.
    (try
      (let [status                 (.getStatus xhr-io)
            log-and-dispatch-error (fn [str error]
                                     ;; note that impl.application/initialize will partially apply the
                                     ;; app-state as the first arg to global-error-callback
                                     (log/error str)
                                     (error-callback error)
                                     (when @global-error-callback
                                       (@global-error-callback status error)))]
        (if (zero? status)
          (log-and-dispatch-error
            (str "UNTANGLED NETWORK ERROR: No connection established.")
            {:type :network})
          (log-and-dispatch-error (str "SERVER ERROR CODE: " status) {})))
      (finally (.dispose xhr-io))))
  net/FulcroNetwork
  (send [this edn ok error]
    (let [xhrio       (make-xhrio)
          ; STEP 1: Convert the request(s) from query notation to REST...
          ; some logic to morph the incoming request into REST (assume you'd factor this out to handle numerous kinds)
          request-ast (-> (prim/query->ast edn) :children first)
          uri         (str "/" (name (:key request-ast)))   ; in this case, posts
          url         (str "http://jsonplaceholder.typicode.com" uri)]
      (js/console.log :REQUEST request-ast :URI uri)
      ; STEP 2: Send the request
      (.send xhrio url "GET")
      ; STEP 3 (see response-ok above)
      (events/listen xhrio (.-SUCCESS EventType) #(net/response-ok this xhrio ok))
      (events/listen xhrio (.-ERROR EventType) #(net/response-error this xhrio error))))
  (start [this] this))

(defn make-rest-network [] (map->Network {}))
