(ns io.hosaka.user.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [yada.yada :as yada]
            [manifold.deferred :as d]
            [io.hosaka.common.db.health :as health]))

(defn get-db-health [health {:keys [response]}]
  (->
   (health/get-health health)
   (d/chain #(if (= (:health %1) "HEALTHY")
               (assoc response :body %1 :status 200)
               (assoc response :body %1 :status 503)))))

(defn build-routes [orchestrator health]
  ["/" [
        ["health"
         (yada/resource {:methods {:get {:response (partial get-db-health health)
                                         :produces "application/json"}}})]
        ]])

(defrecord Handler [orchestrator routes health]
  component/Lifecycle

  (start [this]
    (assoc this :routes (build-routes orchestrator health)))

  (stop [this]
    (assoc this :routes nil)))


(defn new-handler []
  (component/using
   (map->Handler {})
   [:orchestrator :health]))

