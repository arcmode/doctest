(ns doctest.core
  (:require [clojure.test :refer [deftest is]]))

(defn slice-examples
  "Slices a docstring into a sequence of examples.

  => (slice-examples (str \"Some example description.\n\"
                          \"=> actual\n\"
                            \" expected\"))
     '(\" actual\n expected\")"
  [docstr]
  (drop 1 (clojure.string/split docstr #"\n\s*=>")))

(defn wrap-in-parens
  "Takes an example string slice and wraps it into parens.

  => (wrap-in-parens \" foo \")
     \"( foo )\""
  [exa-str]
  (str "(" exa-str ")"))

(def read-as-list
  "Reads a string as it where the content of a list.

  => (read-as-list \"a b c\")
     '(a b c)"
  (comp read-string wrap-in-parens))

(defn make-is-statement
  "Produces assertions from a wrapped example slice.

  => (make-is-statement '((this equals) (that thing)))
     '(clojure.test/is
        (clojure.core/= (this equals)
                        (that thing)))"
  [exa-code-pair]
  `(is (= ~(first exa-code-pair)
          ~(second exa-code-pair))))

(defn test-statement
  "Produces test statements for a given symbol and docstring.

  => (test-statement ['mock (str \"Some example description\n\"
                                 \"=> actual\n\"
                                   \" expected\")])
    '(clojure.test/deftest mock-doc-test
       (clojure.test/is (clojure.core/= actual
                                        expected)))"
  [[sym docstr]]
  (let [examples-strs (slice-examples docstr)
        examples-list (map read-as-list examples-strs)
        is-statements (map make-is-statement examples-list)
        test-name     (symbol (str (name sym) "-doc-test"))]
    `(deftest ~test-name ~@is-statements)))

(defmacro doctest-fn
  "Produces a test definition for a doc-tested symbol.

   Should be using a mock instead of `doctest.core/wrap-in-parens`

  => (macroexpand-1 '(doctest.core/doctest-fn doctest.core/wrap-in-parens))
     '(clojure.test/deftest wrap-in-parens-doc-test
        (clojure.test/is (clojure.core/= (wrap-in-parens \" foo \")
                                         \"( foo )\")))"
  [fn-sym]
  (test-statement [fn-sym (:doc (eval `(meta (var ~fn-sym))))]))

(defmacro doctest-ns
  "Produces test statements for doctested symbols in a given namespace.

  => (macroexpand-1 '(doctest.core/doctest-ns doctest.core-test))
     '(do
        (clojure.test/deftest example-doc-test
          (clojure.test/is (clojure.core/= (example 1 2 3 4)
                                           10))))"
  [ns-sym]
  (let [symbol-docstrings  (filter second (for [[name var] (ns-publics ns-sym)]
                                            [name (:doc (meta var))]))]
    (cons `do (map test-statement symbol-docstrings))))
