(defproject io.hosaka/user "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repositories ^:replace [["releases" "https://artifactory.i.hosaka.io/artifactory/libs-release"]
                           ["snapshots" "https://artifactory.i.hosaka.io/artifactory/libs-snapshot"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.nrepl "0.2.13"]

                 [io.hosaka/common "1.1.3"]

                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.0"]

                 [buddy/buddy-sign "3.0.0.x"]
                 [buddy/buddy-core "1.5.0.x"]

                 [yogthos/config "1.1.1"]

                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]

                 [org.postgresql/postgresql "42.2.2"]
                 ]
  :main ^:skip-aot io.hosaka.user
  :uberjar-name "user.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["env/dev/resources" "resources"]
                   :env {:dev true}}})
