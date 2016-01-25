(ns chat.client.views.pages.inbox
  (:require [om.core :as om]
            [om.dom :as dom]
            [chat.client.store :as store]
            [chat.client.dispatcher :refer [dispatch!]]
            [cljs-uuid-utils.core :as uuid]
            [chat.client.views.threads :refer [thread-view]]))

(defn inbox-page-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "page inbox"}
        (apply dom/div #js {:className "threads"}
          (concat
            [(om/build thread-view
                       {:id (uuid/make-random-squuid)
                        :new? true
                        :tag-ids []
                        :messages []}
                       {:react-key "new-thread"})]
            (map (fn [t] (om/build thread-view t {:key :id}))
                 (let [user-id (get-in @store/app-state [:session :user-id])]
                   ; sort by last message sent by logged-in user, most recent first
                   (->> (select-keys (data :threads) (get-in data [:user :open-thread-ids]))
                        vals
                        (sort-by
                          (comp (partial apply max)
                                (partial map :created-at)
                                (partial filter (fn [m] (= (m :user-id) user-id)))
                                :messages))
                        reverse)))))))))
