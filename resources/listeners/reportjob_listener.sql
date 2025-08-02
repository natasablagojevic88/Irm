CREATE OR REPLACE FUNCTION listener_reportjob()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('reportjob_listener','Report job changed');
    
    return new;

	END;
$function$
;