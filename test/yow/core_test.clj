(ns yow.core-test
  (:use clojure.test
        yow.core))

(deftest test-command
  (testing "Produces a well formed command string"
    (is (= "*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$7\r\nmyvalue\r\n"
           (command "set" "mykey" "myvalue")))))