SELECT t1.title, t2.production_year
FROM title t1
JOIN title t2 ON t1.id = t2.id
WHERE t2.id BETWEEN 200000 AND 400000
LIMIT 10;
