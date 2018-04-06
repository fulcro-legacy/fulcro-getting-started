(defproject fulcrologic/fulcro-getting-started "0.0.1"
  :description "Fulcro Getting Started"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [fulcrologic/fulcro "2.5.0-beta1"]
                 [com.taoensso/timbre "4.10.0"]

                 ; These deps are only needed if you use built-in server components of Fulcro:
                 [http-kit "2.2.0"]
                 [ring/ring-core "1.6.3" :exclusions [commons-codec]]
                 [bk/ring-gzip "0.2.1"]
                 [bidi "2.1.3"]]

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
                                  [org.clojure/tools.namespace "0.3.0-alpha4"]
                                  [figwheel-sidecar "0.5.15"]]}})
