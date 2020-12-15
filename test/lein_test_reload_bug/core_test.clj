(ns lein-test-reload-bug.core-test
  (:require [lein-test-reload-bug.a-protocol :as ap]
            [lein-test-reload-bug.a-deftype :as at]
            [lein-test-reload-bug.b-protocol :as bp]
            [lein-test-reload-bug.b-deftype :as bt]))

;; it's unclear what order `lein test` require's its test files in.
;; assuming here that it's related to the 
(deftest a-test
  ;; if lein-test-reload-bug.a-protocol is loaded after
  ;; lein-test-reload-bug.b-deftype this call will fail
  (ap/a (bt/->B))
  ;; if lein-test-reload-bug.b-protocol is loaded after
  ;; lein-test-reload-bug.a-deftype this call will fail
  (bp/b (at/->A)))
