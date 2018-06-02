(ns io.hosaka.user
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.tools.logging :as log]
            [io.hosaka.common.db.health :refer [new-health]]
            [io.hosaka.common.db :refer [new-database]]
            [io.hosaka.common.server :refer [new-server]]
            [io.hosaka.user.handler :refer [new-handler]]
            [io.hosaka.user.keys :refer [new-keys]]
            [io.hosaka.user.orchestrator :refer [new-orchestrator]]
            )
  (:gen-class))

(defn init-system [env]
  (component/system-map
   :db (new-database "users" env)
   :orchestrator (new-orchestrator)
   :handler (new-handler)
   :server (new-server env)
   :keys (new-keys env)
   :health (new-health env)
   ))

(defonce system (atom {}))

(defonce repl (atom nil))

(defn get-port [port]
  (cond
    (string? port) (try (Integer/parseInt port)
                        (catch Exception e nil))
    (integer? port) port
    :else nil))

(defn -main [& args]
  (let [semaphore (d/deferred)]
    (reset! system (init-system env))

    (swap! system component/start)
    
    (reset! repl (if-let [nrepl-port (get-port (:nrepl-port env))] (nrepl/start-server :port nrepl-port) nil))

    (log/info "User Service booted")
    (deref semaphore)
    (log/info "User Service going down")
    (component/stop @system)
    (swap! repl (fn [server] (do (if server (nrepl/stop-server server)) nil)))

    (shutdown-agents)
    ))
