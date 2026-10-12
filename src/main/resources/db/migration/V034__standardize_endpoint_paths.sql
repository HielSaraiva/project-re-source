UPDATE user_notifications
SET target_path = '/institution/' || substring(target_path FROM 6)
WHERE target_path LIKE '/ong/%';

UPDATE transactional_mail_outbox
SET target_path = '/institution/' || substring(target_path FROM 6)
WHERE target_path LIKE '/ong/%';

UPDATE transactional_mail_outbox
SET target_path = replace(target_path, 'profile=ong', 'profile=institution')
WHERE target_path LIKE '/login?%profile=ong%';
