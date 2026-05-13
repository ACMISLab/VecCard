SELECT mc.company_type_id, COUNT(DISTINCT mc.movie_id)
FROM movie_companies mc
JOIN company_name cn ON mc.company_id = cn.id
GROUP BY mc.company_type_id
LIMIT 10;