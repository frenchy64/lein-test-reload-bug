(prn "loading" 'lein-test-reload-bug.b-deftype)

(ns lein-test-reload-bug.b-deftype
  (:require [lein-test-reload-bug.a-protocol
             :refer [A]]))

(deftype B []
  A
  (a [this]))
