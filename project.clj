(defproject fulcrologic/fulcro-getting-started "0.0.1"
  :description "Fulcro Getting Started"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [fulcrologic/fulcro "2.5.0-alpha5"]]

  :source-paths ["src/main"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]

  :plugins [[lein-cljsbuild "1.1.7"]]

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
                   :dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.15"]]}})
