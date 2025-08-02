CREATE OR REPLACE FUNCTION listener_modeljasperreport()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('modeljasperreport_listen','Model jasper report changed');
    
    return new;

	END;
$function$
;