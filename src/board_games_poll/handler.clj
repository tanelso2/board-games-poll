(ns board-games-poll.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [board-games-poll.actions :as actions]
            [board-games-poll.scheduler :as scheduler]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/current-poll" [] (redirect (actions/get-current-poll-url)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn init
  []
  (println "Initializing Server")
  (actions/create-poll-if-not-exists!)
  (scheduler/setup-scheduler))
