(defproject algrand "0.1.0"
  :description "Code for thinking about algorithmic randomness"
  :url "ttps://github.com/mars0i/algrand"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [incanter/incanter "1.9.3"]
		 [uncomplicate/neanderthal "0.32.0"]
                 ;; https://groups.google.com/forum/#!topic/uncomplicate/fbXYkT4pEkc
                 ;; Dragan wrote: "SLFJ is needed by the Java dependencies, where it is common that a library uses logging internally (for better, or for worse). You can silence it by configuring any no-op logging,
                 ;; such as by adding timbre to your dependencies:"
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                ]
  :plugins  [[lein-with-env-vars "0.2.0"]] ; needed for Neanderthal?
  ;:env-vars {:DYLD_LIBRARY_PATH "/opt/intel/mkl/lib:/opt/intel/lib"}
  :env-vars {:DYLD_LIBRARY_PATH "/usr/local/lib"}

  ; :repl-options {:init-ns algrand.core})
  ; :main algrand.core
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  ; :profiles {:uberjar {:aot :all}}
  ; :target-path "target/%s"
)
