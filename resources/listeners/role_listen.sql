CREATE OR REPLACE FUNCTION listen_role()
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

    perform pg_notify('role_listener',jsonText);

    return new;

	END;
$function$
;