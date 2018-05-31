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
LEFT JOIN users.permissions ON role_permissions.permission_id = permissions.id AND permissions.disabled_on is null
    WHERE roles.enabled AND user_id = CAST(:id AS UUID)

-- :name get-all-permissions-sql :? :*
   SELECT id, description, role_id
     FROM users.permissions
LEFT JOIN users.role_permissions ON permissions.id = role_permissions.permission_id AND enabled
    WHERE disabled_on IS NULL


-- :name add-permission-sql :! :n
INSERT INTO users.permissions(id,  description,  added_by)
                      VALUES(:id, :description, :added_by)
ON CONFLICT (id) DO NOTHING

-- :name grant-role-permission-sql :! :n
INSERT INTO users.role_permissions
(permission_id, role_id, enabled)
VALUES (:permission_id, :role_id, true)
ON CONFLICT (permission_id, role_id) DO UPDATE
SET enabled = true

-- :name revoke-role-permission-sql :! :n
UPDATE users.role_permissions
SET enabled = false
WHERE permission_id = :permission_id AND role_id = :role_id
