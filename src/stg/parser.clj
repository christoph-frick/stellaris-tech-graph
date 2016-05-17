(ns stg.parser
  (:require [instaparse.core :as insta]
            [clojure.walk :refer [postwalk]]))

(def parser
  (insta/parser 
    "
    S = (Expr|comment)*
    <Type> = Symbol|Object|List|String|Boolean|Number|comment
    List = <'{'> Type* <'}'>
    Object = <'{'> (Expr|comment)* <'}'>
    Expr = Symbol Op Type
    Op = '=' | '>'
    Symbol = #'[\\w@_][\\w\\d_]*'
    String = <'\"'> #'[^\"]*' <'\"'>
    Boolean = 'true' | 'false' | 'yes' | 'no'
    Number = #'-?\\d+(\\.\\d+)?'
    <comment> = <#'\\s*#.*\\r?\\n'>
    "
    :auto-whitespace :standard
    )) 

(defn- transform-object
  [& args] 
  (reduce (fn [m [k op v]] 
            (assoc m k (if (= := op) v [op v])))
          {}
          args))

(defn transform-boolean
  [bool]
  (case bool
    "yes" true 
    "true" true 
    false))

(def transformation
  {:S transform-object
   :List vector
   :Object transform-object
   :Symbol keyword
   :Op keyword
   :Expr vector
   :Number #(BigDecimal. %)
   :String str
   :Boolean transform-boolean
   })

(defn parse
  [string]
  (insta/parse parser string))

(defn transform
  [tree]
  (insta/transform transformation tree))

(def parse-transform
  (comp 
    transform 
    parse))

(defn local-symbol?
  [s]
  (and (keyword? s)
       (.startsWith (name s) "@")))

; consider doing less work in `transformation`
(defn expand-locals
  [exprs]
  (let [{:locals true :techs false} (into {} (map (fn [[k v]] [k (into {} v)]) (group-by (fn [[s _]] (local-symbol? s)) exprs))) ; FIXME: there must be a nicer way to group-by maps into maps
        resolve-local #(get locals % (keyword (str "undefined-" (name %))))]
    (postwalk #(if (local-symbol? %) (resolve-local %) %) techs)))

(def load-techs
  (comp 
    expand-locals
    transform
    parse))
