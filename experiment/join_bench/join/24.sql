SELECT mk1.movie_id, mk2.keyword_id
FROM movie_keyword mk1
JOIN movie_keyword mk2 ON mk1.id = mk2.id
WHERE mk2.id BETWEEN 20000 AND 400000
LIMIT 10;