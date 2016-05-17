(ns stg.util
  (:require [clojure.java.io :as io]))

(defmacro load-edn
  [fn]
  (-> fn
      io/resource
      slurp))
