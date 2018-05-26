CREATE TABLE IF NOT EXISTS users.permissions (
       id text NOT NULL,
       description text NOT NULL,
       added_on timestamp without time zone NOT NULL DEFAULT now(),
       added_by uuid NOT NULL,
       disabled_on timestamp without time zone,
       disabled_by uuid,
       servuce uuid NOT NULL,
       PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users.role_permissions (
       permission_id text NOT NULL,
       role_id text NOT NULL,
       enabled boolean NOT NULL DEFAULT true,
       added_on timestamp without time zone NOT NULL DEFAULT now(),
       PRIMARY KEY (permission_id, role_id),
       CONSTRAINT role_permissions_permission FOREIGN KEY (permission_id)
               REFERENCES users.permissions (id) MATCH SIMPLE
               ON UPDATE RESTRICT
               ON DELETE RESTRICT,
       CONSTRAINT role_permissions_role FOREIGN KEY (role_id)
                  REFERENCES users.roles (id) MATCH SIMPLE
                  ON UPDATE RESTRICT
                  ON DELETE RESTRICT);

ALTER TABLE users.users
ADD COLUMN IF NOT EXISTS user_type text;

UPDATE users.users
SET user_type = 'USER'
WHERE user_type IS null;

ALTER TABLE users.users ALTER COLUMN user_type SET NOT NULL;


