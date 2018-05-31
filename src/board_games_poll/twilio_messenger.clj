(ns board-games-poll.twilio-messenger
  (:require [board-games-poll.config :as config])
  (:import com.twilio.Twilio
           (com.twilio.type PhoneNumber)
           (com.twilio.rest.api.v2010.account Message MessageCreator)))

(def ^:private initialized (atom false))

(defn send-message
  [^String message]
  (when (not @initialized)
    (Twilio/init (config/get-twilio-account-sid)
                 (config/get-twilio-auth-token))
    (reset! initialized true))
  (println "Sending out a message")
  (->> message
      (Message/creator (PhoneNumber. (config/get-twilio-recipient-phone))
                       (PhoneNumber. (config/get-twilio-sending-phone)))
      (.create)))
