SELECT cn1.name AS name1, cn2.name AS name2
FROM char_name cn1
JOIN char_name cn2 ON cn1.id = cn2.id
WHERE cn2.id BETWEEN 300000 AND 500000
LIMIT 10;