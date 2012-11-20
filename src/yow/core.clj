(ns yow.core
  (:require [clojure.string :as str])
  (:import (java.net Socket)
           (java.io BufferedInputStream DataInputStream)))

(def crlf "\r\n")

(defn command
  "Constructs a complete command string to send to Redis.
   Conforms to the Redis unified protocol"
  [name & args]
  (str "*"
       (inc (count args)) crlf
       "$" (count name) crlf
       (str/upper-case name) crlf
       (str/join crlf
                 (map (fn [x] (str "$" (count x) crlf x)) args))
       crlf))

(defn- socket
  []
  (doto (Socket. "localhost" 6379)
    (.setTcpNoDelay true)
    (.setKeepAlive true)))

(defn request
  [query]
  (with-open [socket (socket)
              in (DataInputStream. (BufferedInputStream. (.getInputStream socket)))
              out (.getOutputStream socket)]
    (.write out (.getBytes (apply str query)))
    (println in)))