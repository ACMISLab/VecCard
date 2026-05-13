SELECT ci1.person_id, ci2.role_id
FROM cast_info ci1
JOIN cast_info ci2 ON ci1.id = ci2.id
WHERE ci2.id BETWEEN 100000 AND 300000
LIMIT 10;

