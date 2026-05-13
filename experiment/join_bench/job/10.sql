SELECT ak.kind_id, COUNT(DISTINCT t.id)
FROM title t
JOIN aka_title ak ON ak.production_year = t.production_year
WHERE ak.id BETWEEN 177960 AND 377960
GROUP BY ak.kind_id
LIMIT 10;
