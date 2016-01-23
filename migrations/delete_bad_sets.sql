UPDATE draft_pack
SET set_id = query.good_id
FROM (
       SELECT good_sets.id as good_id, bad_sets.id as bad_id, bad_sets.name  
	 FROM (SELECT s.*
		FROM set s
	   LEFT JOIN card c 
		  ON s.id = c.set_id 
	       WHERE c.id IS NULL) as bad_sets
	 JOIN set good_sets
	   ON good_sets.id <> bad_sets.id
	  AND trim(good_sets.name) = trim(bad_sets.name)
) query
WHERE set_id = query.bad_id;

delete from set where id in (select s.id from set s left join card c on s.id = c.set_id where c.id is null and s.name not like '%C0%');

commit;