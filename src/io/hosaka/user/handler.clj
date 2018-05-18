(ns io.hosaka.user.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [yada.yada :as yada]
            [manifold.deferred :as d]
            [io.hosaka.user.orchestrator :as orchestrator]
            [io.hosaka.common.db.health :as health]))

(defn get-db-health [health {:keys [response]}]
  (->
   (health/get-health health)
   (d/chain #(if (= (:health %1) "HEALTHY")
               (assoc response :body %1 :status 200)
               (assoc response :body %1 :status 503)))))

(defn get-user-by-id [orchestrator {:keys [response] :as ctx}]
  (let [id (-> ctx :parameters :path :id)]
    (orchestrator/get-user-by-id orchestrator id)))

(defn get-user-by-login [orchestrator {:keys [response] :as ctx}]
  (let [login (-> ctx :parameters :query :login)]
    (orchestrator/get-user-by-login orchestrator login)))

(defn build-routes [orchestrator health]
  ["/" [
        [["users/" :id]
         (yada/resource {:parameters {:path {:id String}}
                         :methods {:get {:produces "application/json"
                                         :response (partial get-user-by-id orchestrator)}}})]
        ["users"
         (yada/resource {:parameters {:query {:login String}}
                         :methods {:get {:produces "application/json"
                                         :response (partial get-user-by-login orchestrator)}}})]
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

