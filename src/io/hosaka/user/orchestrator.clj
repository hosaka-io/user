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
  (d/let-flow [claims (k/unsign keys token)
               user (get-user-by-id orchestrator (:sub claims))]
    (merge claims user)))

(defn get-all-permissions [{:keys [db]}]
  (users/get-all-permissions db))

(defn grant-role-permission [{:keys [db]} permissions roles user]
  (if (or (empty? permissions)
          (empty? roles))
    (d/success-deferred 0)
    (d/chain
     (apply d/zip
            (doall
             (for [permission permissions
                   role roles]
               (users/grant-role-permission db permission role user))))
     #(reduce + %))))

(defn add-permission [{:keys [db] :as orchestrator} permission user]
  (->
   (users/add-permission db (select-keys permission [:id :description]) user)
   (d/chain
    (fn [c]
      (d/chain
       (grant-role-permission orchestrator (-> permission :id list) (:roles permission) user)
       #(+ c %))))))

