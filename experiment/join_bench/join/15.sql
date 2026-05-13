SELECT cn1.imdb_id, cn2.name_pcode_nf
FROM char_name cn1
JOIN char_name cn2 ON cn1.imdb_id = cn2.imdb_id
WHERE cn2.id BETWEEN 100000 AND 300000
LIMIT 10;
