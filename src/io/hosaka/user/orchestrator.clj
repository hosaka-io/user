(ns io.hosaka.user.orchestrator
  (:require [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
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
  (d/let-flow [user (users/get-user-by-login db login)
               roles-and-permissions (users/get-user-roles-and-permissions db (:id user))]
    (merge user roles-and-permissions)))

(defn get-user-by-id [{:keys [db]} id]
  (d/let-flow [user (users/get-user-by-id db id)
               roles-and-permissions (users/get-user-roles-and-permissions db (:id user))]
    (merge user roles-and-permissions)))
