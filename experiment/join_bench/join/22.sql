SELECT n1.gender, n2.imdb_index
FROM name n1
JOIN name n2 ON n1.id = n2.id
WHERE n2.id BETWEEN 200000 AND 400000
LIMIT 10;
