CREATE OR REPLACE FUNCTION listener_modelcolumn()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('modelcolumn_listen','Model column changed');
    
    return new;

	END;
$function$
;