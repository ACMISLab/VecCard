SELECT t.kind_id, COUNT(DISTINCT ak.id)
FROM title t
JOIN title ak ON t.production_year = ak.production_year
WHERE ak.id <= 300000
GROUP BY t.kind_id
LIMIT 10;