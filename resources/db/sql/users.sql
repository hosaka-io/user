-- db/sql/users.sql

-- :name get-user-by-login-sql :? :*
WITH enabled_user_roles AS (
     SELECT user_roles.role_id,  user_roles.user_id
     FROM users.roles
     JOIN users.user_roles ON user_roles.role_id = roles.id
     WHERE user_roles.enabled AND roles.enabled)
   SELECT users.id, users.name, logins.username, enabled_user_roles.role_id AS "role"
     FROM users.users
     JOIN users.logins ON users.id = logins.user_id
LEFT JOIN enabled_user_roles ON users.id = enabled_user_roles.user_id
    WHERE logins.username = :username

-- :name get-user-by-id-sql :? :*
WITH enabled_user_roles AS (
     SELECT user_roles.role_id,  user_roles.user_id
       FROM users.roles
       JOIN users.user_roles ON user_roles.role_id = roles.id
      WHERE user_roles.enabled AND roles.enabled)
   SELECT users.id, users.name, logins.username, enabled_user_roles.role_id AS "role"
     FROM users.users
     JOIN users.logins ON users.id = logins.user_id AND logins.primary_login
LEFT JOIN enabled_user_roles ON users.id = enabled_user_roles.user_id
    WHERE users.id = CAST(:id AS UUID)
