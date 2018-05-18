(ns io.hosaka.user.db.users
  (:require [manifold.deferred :as d]
            [hugsql.core :as hugsql]
            [io.hosaka.common.db :refer [get-connection]]))

(hugsql/def-db-fns "db/sql/users.sql")

(defn get-user-info [r]
  (if (empty? r)
    nil
    (assoc
     (select-keys (first r) [:id :name :username])
     :roles (set (map :role r)))))

(defn get-user-by-login [db login]
  (->
   (d/future (get-user-by-login-sql (get-connection db) {:username login}))
   (d/chain get-user-info)))

(defn get-user-by-id [db id]
  (->
   (d/future (get-user-by-id-sql (get-connection db) {:id id}))
   (d/catch (fn [e] nil))
   (d/chain get-user-info)))
