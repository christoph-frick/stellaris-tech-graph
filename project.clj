(defproject factorio-graph "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ; [rhizome "0.2.5"]
                 [instaparse "1.4.2"]]
  :main stg.core
  :aliases {"extract" ["trampoline" "run" "-m" "stg.core/extract" "--" ~(str (System/getenv "STELLARIS_HOME") "/common/technology/") "resources/public/tech.edn"]})
