(ns todo.playground)


(let [items (map #(str "#" % "#") (replicate 10 "🚀"))]
  (for [idx (range (count items))]
    (str idx "" (nth items idx))))

