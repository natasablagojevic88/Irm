CREATE OR REPLACE FUNCTION listener_reportgroup()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('reportgroup_listener','Report group change');
    
    return new;

	END;
$function$
;