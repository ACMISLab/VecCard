SELECT t1.kind_id, t2.phonetic_code
FROM title t1
JOIN title t2 ON t1.kind_id = t2.kind_id
WHERE t2.id BETWEEN 100000 AND 300000
LIMIT 10;
