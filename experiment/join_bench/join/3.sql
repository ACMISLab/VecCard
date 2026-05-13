SELECT ci.person_id, COUNT(DISTINCT cn.id) AS role_count
FROM cast_info ci
JOIN char_name cn ON ci.person_role_id = cn.id
WHERE  cn.id BETWEEN 200000 AND 400000
GROUP BY ci.person_id
LIMIT 10;
