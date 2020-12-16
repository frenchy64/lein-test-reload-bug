(prn "loading" 'lein-test-reload-bug.core-test)

(ns lein-test-reload-bug.core-test
  (:require [clojure.test :refer [deftest]]
            [lein-test-reload-bug.a-protocol :as ap]
            [lein-test-reload-bug.a-deftype :as at]
            [lein-test-reload-bug.b-protocol :as bp]
            [lein-test-reload-bug.b-deftype :as bt]))

;; `lein test` seems to load namespace in some unspecified order.
;; I've tried to construct this test to always fail no matter the order,
;; but it might not always fail on your machine.
;;
;; In theory, the bug stems from the fact that
;;   (require :reload 'A 'B)
;; will load B twice if A depends on B.
;; 
;; eg., my machine loads the b-* namespaces first, then 
;;      this sequence of namespaces are loaded (from the top-level prns in each file)
;;
;; 1. "loading" lein-test-reload-bug.a-deftype
;; 2. "loading" lein-test-reload-bug.b-protocol
;; 3. "loading" lein-test-reload-bug.a-protocol
;; 4. "loading" lein-test-reload-bug.b-deftype
;; 5. "loading" lein-test-reload-bug.b-protocol
;; 6. "loading" lein-test-reload-bug.core-test
;;
;; Notice b-protocol is loaded twice:
;; - in step 2 as a dependency of a-deftype
;; - in step 5, presumably by the require :reload in `lein test`'s impl
(deftest a-test
  ;; if lein-test-reload-bug.a-protocol is loaded after
  ;; lein-test-reload-bug.b-deftype this call will fail
  (ap/a (bt/->B))
  ;; if lein-test-reload-bug.b-protocol is loaded after
  ;; lein-test-reload-bug.a-deftype this call will fail
  (bp/b (at/->A)))
