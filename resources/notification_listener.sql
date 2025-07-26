CREATE OR REPLACE FUNCTION notification_listen()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare 
numberOfUnread numeric;
	BEGIN
	
    if TG_OP = 'INSERT' or TG_OP='UPDATE' then 
		select count(*) into numberOfUnread from notification n where n.appuser=new.appuser
	    and unread is true;
	
	    perform pg_notify('notification_listen',
	     json_build_object('operation',TG_OP,'userid',new.appuser,'count',numberOfUnread)::text
	    );
	else 
		select count(*) into numberOfUnread from notification n where n.appuser=old.appuser
	    and unread is true;
	
	    perform pg_notify('notification_listen',
	     json_build_object('operation',TG_OP,'userid',old.appuser,'count',numberOfUnread)::text
	    );
	end if;

    return new;

	END;
$function$