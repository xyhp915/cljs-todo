(ns test.core
  (:require [cljs.test :refer (deftest is)]))


(defn start []
  (is (= 1 1)))

(defn stop [done]
  ; tests can be async. You must call done so that the runner knows you actually finished
  (done))

(defn ^:export init []
  (start))

;(deftest test-failure-test
;  (is (= 100 9))
;  (is (= 1 1)))
