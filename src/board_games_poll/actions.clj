(ns board-games-poll.actions
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [board-games-poll.config :as config]
            [clj-time.core :as t]
            [clj-time.predicates :refer [tuesday?]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.format :as f]))

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

(defn get-next-tuesday
  []
  (->> (periodic-seq (t/now) (t/days 1))
       (filter tuesday?)
       (first)))

(defn format-date
  [date]
  (f/unparse (f/formatters :year-month-day) date))

(defn get-poll-title
  []
  (format "What board games should %s bring? (%s)"
          (config/get-owner-name)
          (format-date (get-next-tuesday))))

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

(defn format-votes-message
  [votes]
  (->> votes
       (sort-by (comp - second))
       (filter (comp #(not= 0 %) second))
       (map-indexed (fn [index [name number]]
                      (format "%d. %s - %d vote%s"
                              (inc index)
                              name
                              number
                              (if (> number 1) "s" ""))))
       (#(if (empty? %) "No votes!" (str/join \newline %)))
       (str "Here are the results:\n")))

(defn get-current-votes-message
  []
  (-> (get-current-votes)
      (format-votes-message)))

(defn get-current-poll-url
  []
  (str "https://www.strawpoll.me/" (get-current-poll-id)))
