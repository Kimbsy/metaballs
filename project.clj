(defproject metaballs "0.1.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [quip "2.0.2"]
                 [com.clojure-goes-fast/clj-async-profiler "1.3.2"]]
  :jvm-opts ["-Djdk.attach.allowAttachSelf" "-XX:+UnlockDiagnosticVMOptions" "-XX:+DebugNonSafepoints"]
  :main ^:skip-aot metaballs.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
