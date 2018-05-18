(ns io.hosaka.user.orchestrator
  (:require [com.stuartsierra.component :as component]
            [io.hosaka.user.db.users :as users]))


(defrecord Orchestrator [db keychain]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (component/using
   (map->Orchestrator {})
   [:db]))

(defn get-user-by-login [{:keys [db]} login]
  (users/get-user-by-login db login))

(defn get-user-by-id [{:keys [db]} id]
  (users/get-user-by-id db id))
