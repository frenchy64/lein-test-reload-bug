(ns lein-test-reload-bug.b-deftype
  (:require [lein-test-reload-bug.a-protocol
             :refer [A]]))

(deftype A []
  A
  (a [this]))
