SELECT n1.imdb_id, n2.surname_pcode
FROM name n1
JOIN name n2 ON n1.imdb_id = n2.imdb_id
WHERE n2.id BETWEEN 100000 AND 300000
LIMIT 10;
