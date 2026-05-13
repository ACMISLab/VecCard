SELECT mc.company_type_id, COUNT(DISTINCT t.id)
FROM title t
JOIN movie_companies mc ON t.id = mc.movie_id
WHERE mc.id BETWEEN 100000 AND 300000
GROUP BY mc.company_type_id
LIMIT 10;
