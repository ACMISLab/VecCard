SELECT mc1.company_type_id, mc2.movie_id
FROM movie_companies mc1
JOIN movie_companies mc2 ON mc1.company_type_id = mc2.company_type_id
WHERE mc2.id BETWEEN 200000 AND 400000
LIMIT 10;