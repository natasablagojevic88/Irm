CREATE OR REPLACE FUNCTION listener_report()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('report_listener','Report change');
    
    return new;

	END;
$function$
;