(ns io.hosaka.user.handler
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.set :as set]
            [yada.yada :as yada]
            [manifold.deferred :as d]
            [io.hosaka.user.orchestrator :as orchestrator]
            [io.hosaka.common.db.health :as health]))

(defn get-user-by-id [orchestrator {:keys [response] :as ctx}]
  (let [id (-> ctx :parameters :path :id)]
    (orchestrator/get-user-by-id orchestrator id)))

(defn get-user-by-login [orchestrator {:keys [response] :as ctx}]
  (let [login (-> ctx :parameters :query :login)]
    (orchestrator/get-user-by-login orchestrator login)))

(defn get-user-from-token [orchestrator {:keys [body]}]
  (orchestrator/get-user-from-token orchestrator body))

(defn secure [orchestrator permissions handler]
  (fn [{:keys [response request] :as ctx}]
    (if-let [header (-> request :headers (get "authorization"))]
      (if-let [token (second (re-matches #"[Bb]earer: (.*)" header))]
        (d/chain
         (d/catch
             (orchestrator/get-user-from-token orchestrator token)
             #(do
                (log/info "Invalid token" %)
                (assoc response :body {:error "Invalid authorization token"} :status 401)))
         (fn [user]
           (if (empty? (set/intersection (:permissions user) permissions))
             (assoc response :body {:error "Incorrect permissions"} :status 403)
             (handler orchestrator (assoc ctx :user user)))))
        (assoc response :body {:error "No authorization token"} :status 401))
      (assoc response :body {:error "No authorization token"} :status 401))))

(defn build-routes [orchestrator health]
  ["/" [
        [["users/" :id]
         (yada/resource {:parameters {:path {:id String}}
                         :methods {:get {:produces "application/json"
                                         :response (secure orchestrator #{"USER_GET_USER_INFO"} get-user-by-id)}}})]
        ["users"
         (yada/resource {:methods {:post {:produces "application/json"
                                          :response (partial get-user-from-token orchestrator)
                                          :consumes "text/plain"}
                                   :get {:produces "application/json"
                                         :parameters {:query {:login String}}
                                         :response (secure orchestrator #{"USER_GET_USER_INFO"} get-user-by-login)}}})]
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

