SELECT pi1.info, pi2.note
FROM person_info pi1
JOIN person_info pi2 ON pi1.id = pi2.id
WHERE pi2.id BETWEEN 100000 AND 300000
LIMIT 10;