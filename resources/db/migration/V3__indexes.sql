CREATE INDEX user_login_primary_login_idx
ON users.logins USING btree
(user_id ASC NULLS LAST)
WHERE primary_login;

CREATE UNIQUE INDEX user_login_u_idx
ON users.logins USING btree
(username);
