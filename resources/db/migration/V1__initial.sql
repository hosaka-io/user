CREATE TABLE IF NOT EXISTS users.users (
       id uuid NOT NULL DEFAULT uuid_generate_v4(),
       name text NOT NULL,
       status text NOT NULL DEFAULT 'NEW',
       added timestamp without time zone NOT NULL DEFAULT now(),
       PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users.logins (
       user_id uuid NOT NULL,
       username text NOT NULL,
       primary_login boolean NOT NULL DEFAULT false,
       PRIMARY KEY (username, user_id),
       CONSTRAINT users_logins_user FOREIGN KEY (user_id)
           REFERENCES users.users (id),
           CONSTRAINT users_primary_login EXCLUDE USING btree (user_id WITH =) WHERE (primary_login) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE IF NOT EXISTS users.roles (
       id text NOT NULL,
       description text NOT NULL,
       enabled boolean NOT NULL DEFAULT true,
       PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users.user_roles (
       user_id uuid NOT NULL,
       role_id text NOT NULL,
       enabled boolean NOT NULL,
       added timestamp without time zone NOT NULL DEFAULT now(),
       PRIMARY KEY (user_id,role_id)
);
