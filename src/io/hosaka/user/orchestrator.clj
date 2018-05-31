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

(defn get-user-by-login [{:keys [db] :as orchestrator} login]
  (try
    (d/chain
     (users/get-user-by-login db login)
     (partial get-roles-and-permissions orchestrator))
    (catch AssertionError e (d/error-deferred e))))

(defn get-user-by-id [{:keys [db] :as orchestrator} id]
  (try
      (d/chain
       (users/get-user-by-id db id)
       (partial get-roles-and-permissions orchestrator))
    (catch AssertionError e (d/error-deferred e))))

(defn get-user-from-token [{:keys [keys] :as orchestrator} token]
  (d/chain
   (k/unsign keys token)
   :sub
   #(get-user-by-id orchestrator %)))

(defn get-all-permissions [{:keys [db]}]
  (users/get-all-permissions db))

(defn add-permission [{:keys [db]} permission user]
  (users/add-permission db permission user))
