(ns io.hosaka.common.db.health
  (:require [manifold.deferred :as d]
            [com.stuartsierra.component :as component]
            [io.hosaka.common.db :refer [get-connection def-db-fns]]))

(def-db-fns "db/sql/health.sql")

(defn get-db-health [db]
  (d/future
    (get-db-health-sql (get-connection db))))


(defrecord Health [db env]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-health [env]
  (component/using
   (map->Health {:env env})
   [:db]))

(defn get-health [{:keys [db env]}]
   (-> (get-db-health db)
       (d/chain #(if (or
                      (nil? %1)
                      (empty? %1))
                   {:health "UNHEALTHY"}
                   {:health "HEALTHY" :db %1}))
       (d/catch (fn [e] {:health "UNHEALTHY" :db (.getMessage e)}))
       (d/chain #(assoc % :environment env)))
   )

