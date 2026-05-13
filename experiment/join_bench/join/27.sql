SELECT mii.info_type_id, COUNT(DISTINCT mi.id)
FROM movie_info mi
JOIN movie_info_idx mii ON mi.movie_id = mii.movie_id
WHERE mii.id BETWEEN 100000 AND 300000
GROUP BY mii.info_type_id
LIMIT 10;
