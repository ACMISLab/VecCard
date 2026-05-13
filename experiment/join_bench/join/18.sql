SELECT mii1.info, mii2.note
FROM movie_info_idx mii1
JOIN movie_info_idx mii2 ON mii1.id = mii2.id
WHERE mii2.id BETWEEN 200000 AND 400000
LIMIT 10;
