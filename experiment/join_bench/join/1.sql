SELECT cn.name AS role_name, an.name AS aka_name
FROM char_name cn
JOIN aka_name an ON cn.id = an.person_id
WHERE cn.id <= 100000
ORDER BY role_name
LIMIT 10;
