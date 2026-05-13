SELECT ci1.movie_id, ci2.person_role_id
FROM cast_info ci1
JOIN cast_info ci2 ON ci1.movie_id = ci2.movie_id
WHERE ci2.id BETWEEN 200000 AND 400000
LIMIT 10;
