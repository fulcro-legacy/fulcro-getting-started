(defproject my-project "0.0.1"
  :description "My Project"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.omcljs/om "1.0.0-beta1"]
                 [awkay/untangled "1.0.0-SNAPSHOT"]]

  :source-paths ["src/main"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]

  :plugins [[lein-cljsbuild "1.1.6"]]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/main" "src/dev"]
                :figwheel     {:on-jsload "cljs.user/refresh"}
                :compiler     {:main          cljs.user
                               :output-to     "resources/public/js/app.js"
                               :output-dir    "resources/public/js/app"
                               :preloads      [devtools.preload]
                               :asset-path    "js/app"
                               :optimizations :none}}]}

  :profiles {:dev {:source-paths ["src/dev" "src/main"]
                   :dependencies [[binaryage/devtools "0.9.2"]
                                  [org.clojure/tools.namespace "0.3.0-alpha4"]
                                  [figwheel-sidecar "0.5.9"]]}})
