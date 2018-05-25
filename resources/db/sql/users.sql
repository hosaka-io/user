-- db/sql/users.sql

-- :name get-user-by-login-sql :? :1
   SELECT users.id, users.name, logins.username
     FROM users.users
     JOIN users.logins ON users.id = logins.user_id
    WHERE logins.username = :username

-- :name get-user-by-id-sql :? :1
   SELECT users.id, users.name, logins.username
     FROM users.users
LEFT JOIN users.logins ON users.id = logins.user_id AND logins.primary_login
    WHERE users.id = CAST(:id AS UUID)

-- :name get-user-roles-and-permissions-sql :? :*
   SELECT permissions.id AS permission, user_roles.role_id AS role
     FROM users.roles
     JOIN users.user_roles ON user_roles.role_id = roles.id AND user_roles.enabled
LEFT JOIN users.role_permissions ON role_permissions.role_id = roles.id and role_permissions.enabled
LEFT JOIN users.permissions ON role_permissions.permission_id = permissions.id
    WHERE roles.enabled AND user_id = CAST(:id AS UUID)
