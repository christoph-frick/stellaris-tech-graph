(ns stg.core
  (:require [cljs.reader :as edn]
            [clojure.set])
  (:require-macros [stg.util :as util]))

(enable-console-print!)

(def raw-techs (edn/read-string (util/load-edn "public/tech.edn")))

(defn cytoscape [cfg]
  ((aget js/window "cytoscape") (clj->js cfg)))

(defn de-keyword [maybe-kw]
  (str (if (keyword? maybe-kw)
         (-> maybe-kw name)
         maybe-kw)))

(defn- id
  [tech]
  (de-keyword (:id tech)))

(defn- gen-tier-id
  [tier]
  (str "tier-" tier))

(defn gen-area-id
  [tier area]
  (str (gen-tier-id tier) "-" area))

(defn tier-cluster [tier]
  {:data {:id (gen-tier-id tier)}})

(defn area-cluster [tier area]
  {:data {:id (gen-area-id tier area)
          :parent (gen-tier-id tier)}})

(defn node [tech]
  {:data {:id (id tech)
          :parent (gen-area-id (:tier tech) (:area tech))
          :classes (de-keyword (:area tech))
          :tier (:tier tech)
          }})

(defn edge [source-tech target-tech]
  (let [sid (if (keyword? source-tech) (de-keyword source-tech) (id source-tech))
        tid (id target-tech)] 
    {:data {:id (str sid "->" tid) 
            :source sid 
            :target tid}}))

(let [lut (into {} (map #(vector (:id %) %)) raw-techs)
      ; tier-clusters (for [tier (distinct (map :tier raw-techs))] (tier-cluster tier))
      ; area-clusters (for [[tier area] (distinct (map (juxt :tier :area) raw-techs))] (area-cluster tier area))
      nodes (for [tech raw-techs] (node tech))
      unknowns (clojure.set/difference (into #{} (mapcat :prerequisites raw-techs)) (into #{} (keys lut)))
      unknown-nodes (for [unknown unknowns] {:data {:id (de-keyword unknown) :tier -1}})
      edges (for [tech raw-techs prereq (:prerequisites tech)] (edge (get lut prereq prereq) tech))] 
  (cytoscape {:container (js/document.getElementById "cy")
              :elements (concat (vec nodes) unknown-nodes edges)
              :style [{:selector "node"
                       :style {:background-color "#666"
                               :label "data(id)"
                               :border-width 3
                               :shape "roundrectangle"
                               :text-valign "center"
                               :width "30em"
                               }}
                      {:selector "node.society"
                       :style {:background-color "#efe"}}
                      {:selector "node.physics"
                       :style {:background-color "#eef"}}
                      {:selector "node.engineering"
                       :style {:background-color "#ffe"}}
                      {:selector ":parent"
                       :style {:background-color "#eee"
                               :opacity "0.33"
                               :label "data(id)"
                               :border-width 1
                               }}
                      {:selector "edge"
                       :style {:width 3
                               :line-color "#ccc"
                               :target-arrow-color "#ccc"
                               :target-arrow-shape "triangle"
                               ; :edge-text-rotation "autorotate"
                               }} 
                      ]
              :layout {:name "breadthfirst"
                       :directed true}
              }))

(defn on-js-reload [])

