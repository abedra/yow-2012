(ns yow.commands
  (:use [clojure.data.json :only (read-str)]))

(defn fetch-redis-commands
  []
  (map first
       (read-str
        (slurp "https://raw.github.com/antirez/redis-doc/master/commands.json"))))