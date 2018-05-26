(ns io.hosaka.user.db.users
  (:require [manifold.deferred :as d]
            [hugsql.core :as hugsql]
            [io.hosaka.common.db :refer [get-connection]]))

(hugsql/def-db-fns "db/sql/users.sql")

(defn get-user-by-login [db login]
  (d/future
    (get-user-by-login-sql (get-connection db) {:username login})))

(defn get-user-by-id [db id]
  (d/future
    (get-user-by-id-sql (get-connection db) {:id id})))

(defn get-user-roles-and-permissions [db id]
  (d/future
    (let [roles-and-permissions (get-user-roles-and-permissions-sql (get-connection db) {:id id})]
      (hash-map
       :roles (set (map :role roles-and-permissions))
       :permissions (set (filter some? (map :permission roles-and-permissions)))))))
