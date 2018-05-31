(ns board-games-poll.config)

(def ^:private config-file "config.edn")

(defn- read-config
  []
  ; TODO: File does not exist exceptions
  (clojure.edn/read-string (slurp config-file)))

(defn- get-config-fun-name
  [name]
  (symbol (str "get-" name)))

(defmacro ^:private defconfig
  [name access]
  `(defn ~(get-config-fun-name name)
     []
     (get-in (read-config) ~access)))

(defconfig board-games [:board-games])

(defconfig twilio-account-sid [:twilio :account-sid])

(defconfig twilio-auth-token [:twilio :auth-token])

(defconfig twilio-sending-phone [:twilio :sending-phone])

(defconfig twilio-recipient-phone [:twilio :recipient-phone])

(defconfig current-poll-file [:current-poll-file])

(defconfig owner-name [:owner-name])
