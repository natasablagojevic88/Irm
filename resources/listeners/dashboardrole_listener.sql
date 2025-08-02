CREATE OR REPLACE FUNCTION listener_dashboardrole()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('dashboardrole_listener','Dashboard role changed');
    
    return new;

	END;
$function$
;