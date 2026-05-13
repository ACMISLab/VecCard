SELECT mk1.keyword_id, mk2.movie_id
FROM movie_keyword mk1
JOIN movie_keyword mk2 ON mk1.keyword_id = mk2.keyword_id
WHERE mk2.id BETWEEN 200000 AND 400000
LIMIT 10;
