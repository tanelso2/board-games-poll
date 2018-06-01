(ns board-games-poll.scheduler
  (:require [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.predicates :refer [tuesday?]]
            [chime]
            [board-games-poll.actions :refer [create-poll! get-current-votes-message]]
            [board-games-poll.twilio-messenger :refer [send-message]]))

; TODO: Make this configurable?
(def time-zone (t/time-zone-for-id "America/Los_Angeles"))

(def MONDAY 1)

(def TUESDAY 2)

(def WEDNESDAY 3)

(def SUNDAY 7)

; Every Wednesday at noon
(defn get-new-poll-schedule
  []
  (periodic-seq
    (.. (t/now)
        (withZone time-zone)
        (withTime 12 0 0 0)
        (withDayOfWeek WEDNESDAY))
    (t/weeks 1)))

; Every Tuesday at 8 am
(defn get-poll-notification-schedule
  []
  (periodic-seq
    (.. (t/now)
        (withZone time-zone)
        (withTime 8 0 0 0)
        (withDayOfWeek TUESDAY))
    (t/weeks 1)))

(defn get-next-tuesday
  []
  (->> (periodic-seq (t/now) (t/days 1))
       (filter tuesday?)
       (first)))

(defn- get-fast-testing-schedule
  []
  (periodic-seq (t/now) (t/minutes 1)))

(defn setup-scheduler
  []
  (let [cancel-new-poll (chime/chime-at
                          (get-new-poll-schedule)
                          (fn [_] (create-poll!)))
        cancel-poll-notification (chime/chime-at
                                   (get-poll-notification-schedule)
                                   (fn [_]
                                       (send-message (get-current-votes-message))))]
    [cancel-new-poll cancel-poll-notification]))
