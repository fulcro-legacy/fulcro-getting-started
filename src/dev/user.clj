(ns user
  (:require
    [figwheel-sidecar.system :as fig]
    app.server
    app.operations
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]))

(def figwheel (atom nil))

(defn start-figwheel
  "Start Figwheel on the given builds, or defaults to build-ids in `figwheel-config`."
  ([]
   (let [figwheel-config (fig/fetch-config)
         props           (System/getProperties)
         all-builds      (->> figwheel-config :data :all-builds (mapv :id))]
     (start-figwheel (keys (select-keys props all-builds)))))
  ([build-ids]
   (let [figwheel-config   (fig/fetch-config)
         default-build-ids (-> figwheel-config :data :build-ids)
         build-ids         (if (empty? build-ids) default-build-ids build-ids)
         preferred-config  (assoc-in figwheel-config [:data :build-ids] build-ids)]
     (reset! figwheel (component/system-map
                        :figwheel-system (fig/figwheel-system preferred-config)
                        :css-watcher (fig/css-watcher {:watch-paths ["resources/public/css"]})))
     (println "STARTING FIGWHEEL ON BUILDS: " build-ids)
     (swap! figwheel component/start)
     (fig/cljs-repl (:figwheel-system @figwheel)))))


; resources is on classpath, and the cljs compiler copies stuff there, so we have to be careful that these extras
; don't get re-scanned when refreshing the server.
(set-refresh-dirs "src/dev" "src/main")

(def system (atom nil))
(declare reset)

(defn refresh
  "Refresh the live code. Use this if the server is stopped (e.g. you used `reset` but there was
  a compiler error). Otherwise, use `reset`."
  [& args]
  (if @system
    (println "The server is running. Use `reset` instead.")
    (apply tools-ns/refresh args)))

(defn stop
  "Stop the currently running server."
  []
  (when @system
    (swap! system component/stop))
  (reset! system nil))

(defn go
  "Start the server. Optionally supply a path to your desired config. Relative paths will scan classpath. Absolute
  paths will come from the filesystem. The default is config/dev.edn."
  ([] (go :dev))
  ([path]
   (if @system
     (println "The server is already running. Use reset to stop, refresh, and start.")
     (letfn [(start []
              (swap! system component/start))
            (init [path]
              (when-let [new-system (app.server/make-system "config/dev.edn")]
                (reset! system new-system)))]
      (init path)
      (start)))))

(defn reset
  "Stop the server, refresh the code, and restart the server."
  []
  (stop)
  (refresh :after 'user/go))
