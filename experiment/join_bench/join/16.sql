SELECT mc1.company_id, mc2.note
FROM movie_companies mc1
JOIN movie_companies mc2 ON mc1.id = mc2.id
WHERE mc2.id BETWEEN 200000 AND 400000
LIMIT 10;