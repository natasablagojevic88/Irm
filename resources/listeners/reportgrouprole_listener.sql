CREATE OR REPLACE FUNCTION listener_reportgrouprole()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('reportgrouprole_listener','Report group role changed');
    
    return new;

	END;
$function$
;