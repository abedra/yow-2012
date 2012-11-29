(ns yow.core
  (:refer-clojure :exclude [set get])
  (:use [clojure.data.json :only (read-str)])
  (:require [clojure.string :as str])
  (:import (java.net Socket)
           (java.io BufferedInputStream DataInputStream)))

(defn command
  "Constructs a complete command string to send to Redis.
   Conforms to the Redis unified protocol"
  [name & args]
  (let [crlf "\r\n"]
    (str "*"
         (inc (count args)) crlf
         "$" (count name) crlf
         (str/upper-case name) crlf
         (str/join crlf
           (map (fn [x] (str "$" (count x) crlf x)) args))
         crlf)))

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
  (throw (UnsupportedOperationException.
          "Not Yet Implemented")))

(defn request
  [command]
  (with-open [socket (socket)
              in (DataInputStream.
                  (BufferedInputStream.
                   (.getInputStream socket)))
              out (.getOutputStream socket)]
    (.write out (.getBytes (apply str command)))
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

(defcommands
  (set  [key value])
  (get  [key])
  (incr [key])
  (del  [key & keys]))

(defmacro define-command [command]
  (let [command (eval command)
        name (.toLowerCase (first command))
        data (second command)
        docstring (str (data "summary") "\n" "Complexity: " (data "complexity"))
        arguments (vec (map symbol (map #(% "name") (data "arguments"))))
        varargs? (some #(= % "multiple") (flatten (map keys (data "arguments"))))
        since (data "since")
        group (data "group")]
    `(defn ~(symbol name) ~docstring ~arguments
       (apply command ~name ~arguments))))

(defn fetch-redis-commands
  []
  (read-str
   (slurp "https://raw.github.com/antirez/redis-doc/master/commands.json")))