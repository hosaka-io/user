(ns io.hosaka.user.db.users
  (:require [manifold.deferred :as d]
            [hugsql.core :as hugsql]
            [clojure.spec.alpha :as s]
            [io.hosaka.common.spec :as specs]
            [io.hosaka.common.db :refer [get-connection] :as db]))

(hugsql/def-db-fns "db/sql/users.sql")

(defn get-user-by-login [db login]
  {:pre [(s/valid? ::db/db db)
         (s/valid? ::specs/non-empty-string)]}
  (d/future
    (get-user-by-login-sql (get-connection db) {:username login})))

(defn get-user-by-id [db id]
  {:pre [(s/valid? ::db/db db)
         (s/valid? ::specs/uuid id)]}
  (d/future
    (get-user-by-id-sql (get-connection db) {:id id})))

(defn get-user-roles-and-permissions [db id]
  {:pre [(s/valid? ::db/db db)
         (s/valid? (s/or ::specs/uuid #(instance? java.util.UUID %)) id)
         ]}
  (d/future
    (let [roles-and-permissions (get-user-roles-and-permissions-sql (get-connection db) {:id id})]
      (hash-map
       :roles (set (map :role roles-and-permissions))
       :permissions (set (filter some? (map :permission roles-and-permissions)))))))

(defn get-all-permissions [db]
  {:pre [(s/valid? ::db/db db)]}
  (d/future
    (map
     (fn [permision-and-role]
       (assoc
        (select-keys (first permision-and-role) [:id :description])
        :roles (set (filter some? (map :role_id permision-and-role)))))
     (partition-by :id (get-all-permissions-sql (get-connection db))))))
