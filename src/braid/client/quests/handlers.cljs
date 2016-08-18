(ns braid.client.quests.handlers
  (:require [braid.client.state.handler.core :refer [handler]]
            [braid.client.quests.helpers :as helpers]
            [braid.client.sync :as sync]
            [cljs-uuid-utils.core :as uuid]))

(defn when-> [state bool f]
  (when bool (f))
  state)

(defn make-quest-record [{:keys [quest-id]}]
  {:id (uuid/make-random-squuid)
   :state :active
   :progress 0
   :quest-id quest-id})

(defn activate-next-quest [state {:keys [local-only?]}]
  (if-let [quest (helpers/get-next-quest state)]
    (let [quest-record (make-quest-record {:quest-id (quest :id)})]
      (-> state
          (helpers/store-quest-record quest-record)
          (when-> (not local-only?)
                  (fn [] (sync/chsk-send! [:braid.server.quests/store-quest-record quest-record])))))
    state))

(defmethod handler :quests/skip-quest [state [_ {:keys [quest-record-id local-only?]}]]
  (-> state
      (helpers/skip-quest quest-record-id)
      (when-> (not local-only?)
              (fn [] (sync/chsk-send! [:braid.server.quests/skip-quest quest-record-id])))
      (activate-next-quest {:local-only? local-only?})))

(defmethod handler :quests/complete-quest [state [_ {:keys [quest-record-id local-only?]}]]
  (-> state
      (helpers/complete-quest quest-record-id)
      (when-> (not local-only?)
              (fn [] (sync/chsk-send! [:braid.server.quests/complete-quest quest-record-id])))
      (activate-next-quest {:local-only? local-only?})))
