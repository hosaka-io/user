(ns io.hosaka.user.orchestrator
  (:require [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [io.hosaka.user.keys :as k]
            [io.hosaka.user.db.users :as users]))


(defrecord Orchestrator [db keys]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-orchestrator []
  (component/using
   (map->Orchestrator {})
   [:db :keys]))

(defn get-roles-and-permissions [{:keys [db]} user]
  (d/chain
   (users/get-user-roles-and-permissions db (:id user))
   #(merge user %)))

(defn get-user-by-login [{:keys [db]} login]
  (d/chain
   (users/get-user-by-login db login)
   get-roles-and-permissions))

(defn get-user-by-id [{:keys [db]} id]
  (d/chain
   (users/get-user-by-id db id)
   get-roles-and-permissions))

(defn get-user-from-token [{:keys [db keys]} token]
  (d/chain
   (k/unsign keys token)
   :sub
   get-user-by-id))
