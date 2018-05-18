ALTER TABLE users.user_roles
ADD CONSTRAINT user_roles_user_fk FOREIGN KEY (user_id)
REFERENCES users.users (id) MATCH SIMPLE;

ALTER TABLE users.user_roles
ADD CONSTRAINT user_roles_role_fk FOREIGN KEY (role_id)
REFERENCES users.roles (id) MATCH SIMPLE;

ALTER TABLE users.logins
RENAME CONSTRAINT users_logins_user TO users_logins_user_fk;

