(ns io.hosaka.user
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log]
            [io.hosaka.common.db.health :refer [new-health]]
            [io.hosaka.common.db :refer [new-database]]
            [io.hosaka.common.server :refer [new-server]]
            [io.hosaka.user.handler :refer [new-handler]]
            [io.hosaka.user.orchestrator :refer [new-orchestrator]]
            )
  (:gen-class))

(defonce system (atom {}))

(defn init-system [env]
  (component/system-map
   :db (new-database "users" env)
   :orchestrator (new-orchestrator)
   :handler (new-handler)
   :server (new-server env)
   :health (new-health env)
   ))

(defn -main [& args]
  (let [semaphore (d/deferred)]
    (reset! system (init-system env))

    (swap! system component/start)
    (log/info "User Service booted")
    (deref semaphore)
    (log/info "User Service going down")
    (component/stop @system)

    (shutdown-agents)
    ))
