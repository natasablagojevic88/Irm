CREATE OR REPLACE FUNCTION listener_dashboard()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('dashboard_listener','Dashboard changed');
    
    return new;

	END;
$function$
;