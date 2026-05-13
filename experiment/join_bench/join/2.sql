SELECT ct.kind, COUNT(mc.movie_id)
FROM movie_companies mc
JOIN company_type ct ON mc.company_type_id = ct.id
GROUP BY ct.kind;
