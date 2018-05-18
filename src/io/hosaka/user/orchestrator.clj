(ns io.hosaka.user.orchestrator
  (:require [com.stuartsierra.component :as component]))


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

