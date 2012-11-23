(ns yow.core-test
  (:use clojure.test
        yow.core))

(deftest test-command
  (testing "Produces a well formed command string"
    (is (= "*3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$7\r\nmyvalue\r\n"
           (command "set" "mykey" "myvalue")))))

(deftest test-basic-interaction
  (testing "SET then GET"
    (is (= "OK" (request (command "set" "server:name" "fido"))))
    (is (= "fido" (request (command "get" "server:name")))))
  (testing "INCR"
    (request (command "set" "connections" "10"))
    (is (= 11 (request (command "incr" "connections")))))
  (testing "DEL"
    (is (= 1 (request (command "del" "connections"))))))