-- Roles
INSERT INTO roles (id, name) VALUES (1, 'admin') ON CONFLICT DO NOTHING;
INSERT INTO roles (id, name) VALUES (2, 'user') ON CONFLICT DO NOTHING;
INSERT INTO roles (id, name) VALUES (3, 'editor') ON CONFLICT DO NOTHING;

-- Permissions
INSERT INTO permissions (id, resource, action, name) VALUES (1, 'users', 'read', 'users:read') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (2, 'users', 'create', 'users:create') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (3, 'users', 'update', 'users:update') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (4, 'users', 'delete', 'users:delete') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (5, 'snippets', 'read', 'snippets:read') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (6, 'snippets', 'create', 'snippets:create') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (7, 'snippets', 'update', 'snippets:update') ON CONFLICT DO NOTHING;
INSERT INTO permissions (id, resource, action, name) VALUES (8, 'snippets', 'delete', 'snippets:delete') ON CONFLICT DO NOTHING;

-- Admin role permissions (all)
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8) ON CONFLICT DO NOTHING;

-- User role permissions (read only)
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1), (2, 5) ON CONFLICT DO NOTHING;

-- Editor role permissions (read and write)
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 1), (3, 5), (3, 6), (3, 7) ON CONFLICT DO NOTHING;
