SELECT n.id, COUNT(DISTINCT pi.info_type_id)
FROM name n
JOIN person_info pi ON n.id = pi.person_id
WHERE pi.id BETWEEN 200000 AND 400000
GROUP BY n.id
LIMIT 10;