SELECT an1.name AS name1, an2.name AS name2
FROM aka_name an1
JOIN aka_name an2 ON an1.person_id = an2.person_id
WHERE an2.id BETWEEN 300000 AND 400000
LIMIT 10;
