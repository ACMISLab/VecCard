SELECT mii1.info_type_id, mii2.movie_id
FROM movie_info_idx mii1
JOIN movie_info_idx mii2 ON mii1.info_type_id = mii2.info_type_id
WHERE mii2.id BETWEEN 100000 AND 300000
LIMIT 10;