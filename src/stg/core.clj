(ns stg.core
  (:require [clojure.java.io :as io]
            ; [clojure.string :as str]
            ; [rhizome.viz :refer [view-graph]]
            ; [rhizome.dot :as dot]
            [stg.parser :refer [load-techs]]))

(defn- cleanup
  [[id tech]]
  (assoc tech 
         :id id
         :prerequisites (mapv keyword (:prerequisites tech))))

(defn file
  [file-name]
  (.getCanonicalFile (io/file file-name)))

(defn tech?
  [obj]
  (and (map? obj)
       (contains? obj :area)))

(defn load-all-techs
  [dir-name]
  (let [dir (file dir-name)] 
    (assert (and (.isDirectory dir) (.canRead dir)) (str "Not readable directory: " dir))
    (sequence 
      (comp 
        (filter #(.isFile %))
        (mapcat #(load-techs (slurp %)))
        (filter (fn [[k v]] (tech? v)))
        (map cleanup))
      (file-seq dir))))

;;; just for local testing
; (defn adjacencies
;   [id-fn prerequisites-fn nodes]
;   (let [lut (into {} (map #(vector (id-fn %) %) nodes))] 
;     (reduce-kv 
;       (fn [m k v] (assoc m k (mapv second v)))
;       {}
;       (group-by first 
;                 (mapcat (fn [node] 
;                           (mapv #(vector (get lut %) node) (prerequisites-fn node))) 
;                         nodes)))))
; 
; #_(adjacencies :id :deps [{:id 1 :deps [2 3]} {:id 2} {:id 3}])
; 
; (defn pretty-tech-name
;   [tech-kw]
;   (apply str (-> tech-kw name (str/replace #"(?i)(?:tech)?_(\w)" (fn [[_ c]] (str " " (.toUpperCase c)))))))
; 
; #_(let [nodes techs] 
;     (view-graph
;       nodes
;       (adjacencies :id :prerequisites nodes)
;       :options {:node {:fontsize 8 :fontname "Fira" :shape "rectangle"}
;                 :edge {:fontsize 8 :fontname "Fira"}}
;       :vertical? false
;       :node->descriptor (fn [tech] {:label (-> tech :id pretty-tech-name)})
;       ; :node->cluster (fn [tech] (select-keys tech [:tier :area]))
;       ; :cluster->parent (fn [cluster] (when (contains? cluster :area) (select-keys cluster [:tier])))
;       ; :cluster->descriptor (fn [{:keys [tier area]}] (if area {:label (name area)} {:label (str "Tier " tier)}))
;       :node->cluster :tier
;       :cluster->descriptor (fn [tier] {:label (str "Tier " tier)})
;       ))

(defn write-edn
  [out-file-name data]
  (let [f (file out-file-name)]
    (spit f (pr-str data))))

(defn extract
  [dir-name out-file-name]
  (->> (load-all-techs dir-name)
       (write-edn out-file-name)))

(defn -main
  [& args]
  (assert (= (count args) 2))
  (extract (first args) (second args)))
