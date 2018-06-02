(ns io.hosaka.user.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [yada.yada :as yada]
            [manifold.deferred :as d]
            [io.hosaka.user.orchestrator :as orchestrator]
            [io.hosaka.common.db.health :as health])
  (:import (java.util UUID)))

(defn get-user-by-id [orchestrator {:keys [response] :as ctx}]
  (let [id (-> ctx :parameters :path :id)]
    (orchestrator/get-user-by-id orchestrator id)))

(defn get-user-by-login [orchestrator {:keys [response] :as ctx}]
  (let [login (-> ctx :parameters :query :login)]
    (orchestrator/get-user-by-login orchestrator login)))

(defn get-user-from-token [orchestrator {:keys [body response]}]
  (d/catch
      (orchestrator/get-user-from-token orchestrator body)
      (fn [e]
        (log/info "Invalid authorization token" e)
        (assoc response :body {:error "Invalid authorization token"} :status 401))))

(defn get-all-permissions [orchestrator ctx]
  (orchestrator/get-all-permissions orchestrator))

(defn add-permission [orchestrator {:keys [body response user]}]
  (let [user-id (:sub user)
        permissions (if (map? body) (vector body) body)]
    (if (or (empty? (mapcat :roles permissions)) (contains? (:permissions user) "USER_ASSIGN_PERMISSION"))
      (d/chain
       (apply d/zip
              (doall
               (map #(orchestrator/add-permission orchestrator % user-id) permissions)))
       #(reduce + %)
       #(if (< 0 %) {:status "added" :cnt %} {:status "not added"}))
      (assoc response :body {:error "Incorrect permissions"} :status 403))))

(defn secure [orchestrator permissions handler]
  (fn [{:keys [response request] :as ctx}]
    (if-let [header (-> request :headers (get "authorization"))]
      (if-let [token (second (re-matches #"[Bb]earer: (.*)" header))]
        (->
         (orchestrator/get-user-from-token orchestrator token)
         (d/catch #(do (log/info "Invalid token" %)
                       (assoc response :body {:error "Invalid authorization token"} :status 401)))
         (d/chain (fn [user]
                    (if (empty? (set/intersection (:permissions user) permissions))
                      (assoc response :body {:error "Incorrect permissions"} :status 403)
                      (handler orchestrator (assoc ctx :user user))))))
        (assoc response :body {:error "No authorization token"} :status 401))
      (assoc response :body {:error "No authorization token"} :status 401))))

(defn build-routes [orchestrator health]
  ["/" [
        [["users/" :id]
         (yada/resource {:parameters {:path {:id UUID}}
                         :methods {:get {:produces "application/json"
                                         :response (partial get-user-by-id orchestrator)}}})]
        ["users"
         (yada/resource {:methods {:post {:produces "application/json"
                                          :response (partial get-user-from-token orchestrator)
                                          :consumes "text/plain"}
                                   :get {:produces "application/json"
                                         :parameters {:query {:login String}}
                                         :response (secure orchestrator #{"USER_GET_USER_INFO"} get-user-by-login)}}})]
        ["permissions"
         (yada/resource {:methods
                         {:post {:produces "application/json"
                                 :consumes "application/json"
                                 :response (secure orchestrator #{"USER_ADD_PERMISSION"} add-permission)}
                          :get
                          {:produces "application/json"
                           :response (secure orchestrator #{"USER_GET_ALL_PERMISSION"} get-all-permissions)}}})]
        ["health"
         (yada/resource {:methods {:get {:response (partial health/get-health health)
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

