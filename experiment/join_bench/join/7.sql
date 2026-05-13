SELECT mk1.keyword_id AS k1, COUNT(*)
FROM movie_keyword mk1
JOIN movie_keyword mk2 ON mk1.movie_id = mk2.movie_id
WHERE mk2.id <=20000
GROUP BY k1
LIMIT 10;