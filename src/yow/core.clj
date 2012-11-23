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

(defmulti response
  (fn [in] (char (.readByte in))))

(defmethod response \- [in]
  (.readLine in))

(defmethod response \+ [in]
  (.readLine in))

(defmethod response \: [in]
  (Long/parseLong (.readLine in)))

(defmethod response \$ [in]
  (.readLine in)
  (.readLine in))

(defmethod response \* [in]
  (throw (UnsupportedOperationException. "Not Yet Implemented")))

(defn request
  [query]
  (with-open [socket (socket)
              in (DataInputStream. (BufferedInputStream. (.getInputStream socket)))
              out (.getOutputStream socket)]
    (.write out (.getBytes (apply str query)))
    (response in)))

(defn parameters
  [params]
  (let [[args varargs] (split-with #(not= '& %)  params)]
    (conj (vec args) (last varargs))))

(defmacro defcommand
  [name params]
  (let [com (str name)
        p (parameters params)]
    `(defn ~name ~params
       (apply command ~com ~@p))))

(defmacro defcommands
  [& commands]
  `(do ~@(map (fn [q] `(defcommand ~@q)) commands)))