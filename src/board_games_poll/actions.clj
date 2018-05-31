(ns board-games-poll.actions
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [board-games-poll.config :as config]))

(def URL "https://www.strawpoll.me/api/v2/polls")

(defn get-current-poll-id
  []
  ; TODO: Handle file does not exist errors
  (slurp (config/get-current-poll-file)))

(defn set-current-poll-id!
  [id]
  (spit (config/get-current-poll-file) id))

(defn get-poll-choices
  []
  (sort (config/get-board-games)))

(defn get-poll-title
  []
  ; TODO: Add dates and shit?
  (str "What board games should "
       (config/get-owner-name)
       " bring?"))

(defn make-poll
  [title choices]
  (let [headers {"Content-Type" "application/json"}
        body {:title   title
              :options choices
              :multi   true}
        body-str (json/write-str body)
        result @(http/post URL {:headers headers :body body-str})
        result-body (json/read-str (:body result))]
    (get result-body "id")))

(defn get-votes
  [id]
  (let [full-url (str URL "/" id)
        result @(http/get full-url)
        body (json/read-str (:body result))
        choices (get body "options")
        votes (get body "votes")]
    (zipmap choices votes)))

(defn create-poll!
  []
  (println "Creating new poll")
  (let [title (get-poll-title)
        choices (get-poll-choices)
        id (make-poll title choices)]
    (set-current-poll-id! id)))

(defn create-poll-if-not-exists!
  []
  (when (not
          (.exists
            (clojure.java.io/as-file (config/get-current-poll-file))))
    (create-poll!)))

(defn get-current-votes
  []
  (get-votes (get-current-poll-id)))

(defn get-current-poll-url
  []
  (str "https://www.strawpoll.me/" (get-current-poll-id)))
