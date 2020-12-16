(prn "loading" 'lein-test-reload-bug.a-protocol)

(ns lein-test-reload-bug.a-protocol)

(defprotocol A
  (a [this]))
