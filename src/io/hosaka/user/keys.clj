(ns io.hosaka.user.keys
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :refer [reader]]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [cheshire.core :as json]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as keys]
            [buddy.sign.jws :refer [decode-header]]
            [byte-streams :as bs]))


(defrecord Keys [env keys]
  component/Lifecycle

  (start [this]
    (assoc this
           :keys (atom {})))

  (stop [this]
    (assoc this :keys nil)))

(defn new-keys [env]
  (map->Keys {:env (select-keys env [:keychain-url])}))

(defn parse-stream [stream]
  (with-open [rdr (reader stream)]
    (json/parse-stream rdr true)))

(defn map-jwk [jwks]
  (apply
   hash-map
   (mapcat
    #(vector (:kid %1) (assoc %1 :key (keys/jwk->public-key %1)))
    jwks)))

(defn get-kid [jwt]
  (->
   jwt
   decode-header
   :kid))

(defn get-key [{:keys [keys env]} kid]
  (if (contains? @keys kid)
    (d/success-deferred (get @keys kid))
    (d/chain
     (http/get (:keychain-url env)) ;;Get JWKs
     :body                           ;;Response body
     parse-stream                    ;;Body JSON -> map
     :keys                           ;;Keys
     map-jwk                         ;;Parse keys
     (fn [jwks]
       (swap! keys #(merge % jwks))
       (if (contains? jwks kid)
         (get jwks kid)
         (throw (Exception. (str "KID: " kid " not found"))))))))

(defn unsign [keys token]
  (d/chain
   (->> token get-kid (get-key keys))
   #(jwt/unsign token (:key %) {:alg :es256})))
