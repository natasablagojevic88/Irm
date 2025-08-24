CREATE OR REPLACE FUNCTION listener_reportjasper()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
declare 
jsonText text;
	BEGIN
	
	if TG_OP = 'INSERT' or TG_OP='UPDATE' then 
		select to_json(t.*)::TEXT into jsonText from (select new.id,TG_OP action) as t;
    else 
		select to_json(t.*)::TEXT into jsonText from (select old.id,TG_OP action) as t;
	end if;

    perform pg_notify('reportjasper_listen',jsonText);
    
    return new;

	END;
$function$
;