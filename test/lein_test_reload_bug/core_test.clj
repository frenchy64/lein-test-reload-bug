(prn "loading" 'lein-test-reload-bug.core-test)

(ns lein-test-reload-bug.core-test
  (:require [clojure.test :refer [deftest]]
            [lein-test-reload-bug.a-deftype :refer [->A]]
            [lein-test-reload-bug.b-protocol :refer [b]]))

;; `lein test` seems to load namespaces in lexicographic order.
;; I'm using that assumption to make this reproduction.
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
;; 3. "loading" lein-test-reload-bug.b-protocol
;; 4. "loading" lein-test-reload-bug.core-test
;;
;; Notice b-protocol is loaded twice:
;; - in step 2 as a dependency of a-deftype
;; - in step 3, presumably by the require :reload in `lein test`'s impl
;; 
;; Since a-deftype is loaded only once, it keeps the stale protocol reference
;; from step 2, which triggers the usual namespace reloading problems.
(deftest a-test
  (let [a (->A)]
    (prn "The current hash of interface lein_test_reload_bug.b_protocol.B is" (hash lein_test_reload_bug.b_protocol.B))
    (prn "The current instance of A implements lein_test_reload_bug.b_protocol.B with hash"
         (-> (into {}
                   (map (juxt #(.getName ^Class %) hash))
                   (-> a class supers))
             (get "lein_test_reload_bug.b_protocol.B")
             (doto (-> assert class?))
             hash))
    (b a)))
