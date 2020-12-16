(prn "loading" 'lein-test-reload-bug.core-test)

(ns lein-test-reload-bug.core-test
  (:require [clojure.test :refer [deftest]]
            [lein-test-reload-bug.a-deftype :refer [->A]]
            [lein-test-reload-bug.b-protocol :refer [b]]))

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
