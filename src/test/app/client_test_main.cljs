(ns app.client-test-main
  (:require app.tests-to-run
            [fulcro-spec.selectors :as sel]
            [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite client-tests {:ns-regex #"app..*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

