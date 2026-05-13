SELECT t1.production_year,COUNT(*)
FROM title t1
JOIN title t2 ON t1.production_year = t2.production_year
WHERE t2.id <= 100000
GROUP BY t1.production_year
LIMIT 10;