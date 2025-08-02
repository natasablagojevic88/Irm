CREATE OR REPLACE FUNCTION listener_model()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

    perform pg_notify('model_listen','Model changed');
    
    return new;

	END;
$function$
;