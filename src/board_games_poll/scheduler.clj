(ns board-games-poll.scheduler
  (:require [clj-time.core :as t]
            [clj-time.periodic]
            [chime]
            [board-games-poll.actions :refer [create-poll! get-current-votes]]
            [board-games-poll.twilio-messenger :refer [send-message]]))

; TODO: Make this configurable?
(def time-zone (t/time-zone-for-id "America/Los_Angeles"))

(def MONDAY 1)

(def TUESDAY 2)

(def SUNDAY 7)

; Every Monday at noon
(defn get-new-poll-schedule
  []
  (clj-time.periodic/periodic-seq
    (.. (t/now)
        (withZone time-zone)
        (withTime 12 0 0 0)
        (withDayOfWeek MONDAY))
    (t/weeks 1)))

; Every Tuesday at 8 am
(defn get-poll-notification-schedule
  []
  (clj-time.periodic/periodic-seq
    (.. (t/now)
        (withZone time-zone)
        (withTime 8 0 0 0)
        (withDayOfWeek TUESDAY))
    (t/weeks 1)))

(defn setup-scheduler
  []
  (let [cancel-new-poll (chime/chime-at
                          (get-new-poll-schedule)
                          (fn [_] (create-poll!)))
        cancel-poll-notification (chime/chime-at
                                   (get-poll-notification-schedule)
                                   (fn [_]
                                     (let [votes (get-current-votes)]
                                       (send-message (str votes)))))]
    [cancel-new-poll cancel-poll-notification]))
