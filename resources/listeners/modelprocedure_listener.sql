CREATE OR REPLACE FUNCTION listener_modelprocedure()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('modelprocedure_listener','Model procedure changed');
    
    return new;

	END;
$function$
;