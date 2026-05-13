SELECT an1.name AS name1, an2.name AS name2
FROM aka_name an1
JOIN aka_name an2 ON an1.id = an2.id
WHERE an2.id BETWEEN 100000 AND 200000
LIMIT 10;

