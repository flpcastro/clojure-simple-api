(ns clojure-api.server
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]))

(def store (atom {}))
; example:
; {id {task_id task_name task_status} }

(defn create-task-map [name status]
  {:name name :status status})

(defn create-task [request]
  (let [uuid (java.util.UUID/randomUUID)
        name (get-in request [:query-params :name])
        status (get-in request [:query-params :status])
        task (create-task-map name status)]
    (swap! store assoc uuid task)
    {:status 200 :body {:message "Task created successfully"
                        :task    task}}))


(defn hello [request]
  {:status 200 :body (str "Hello World " (get-in request [:query-params :name] "Everybody!"))})

(def routes (route/expand-routes
              #{["/hello" :get hello :route-name :hello-world]
                ["/task" :post create-task :route-name :create-task]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))

(defn start-server []
  (reset! server (http/start (http/create-server service-map))))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)
(println (test-request :get "/hello?name=Felipe"))
(println (test-request :post "/task?name=Run&status=peding"))

; (println @store)
