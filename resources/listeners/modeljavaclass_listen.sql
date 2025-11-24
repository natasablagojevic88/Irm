CREATE OR REPLACE FUNCTION listen_modeljavaclass()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
declare
jsonText text;
	BEGIN

    if TG_OP = 'INSERT' or TG_OP='UPDATE' then 
		select to_json(t.*)::TEXT into jsonText from (select new.*,TG_OP action) as t;
    else 
		select to_json(t.*)::TEXT into jsonText from (select old.*,TG_OP action) as t;
	end if;

    perform pg_notify('modeljavaclass_listen',jsonText);

    return new;

	END;
$function$
;