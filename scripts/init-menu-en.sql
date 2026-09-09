-- =============================================
-- Menu Data (English Version)
-- 仅包含有效菜单数据 (is_deleted = 0)
-- =============================================

-- System Management (Parent)
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1393000177613242368, 'System Management', ':sys-manager', '/sys-manager', 0, 0, 1, 0, '', 0, 'Grid', 2, 1699771308310499328, '2025-07-10 22:45:23', 1699771308310499328, '2025-12-08 12:16:31', 0, NULL);

-- System Home
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1150563411119063040, 'System Home', ':admin:home', '/admin/home', 0, 1, 1, 0, '', 0, 'HomeFilled', 1, 1699771308310499328, '2023-09-10 22:48:14', 1699771308310499328, '2025-12-08 12:16:09', 0, NULL);

-- Personal Center
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1150129636463624192, 'Personal Center', ':admin:user-center', '/admin/sys-user-center', 0, 1, 1, 1, '', 0, 'Apple', 98, 1699771308310499328, '2023-09-09 18:04:34', 1699771308310499328, '2025-12-08 12:09:33', 0, NULL);

-- Service Monitoring
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1150560507641479168, 'Service Monitoring', ':admin:dashboard', '/admin/dashboard', 0, 1, 1, 0, '', 0, 'Monitor', 99, 1699771308310499328, '2023-09-10 22:36:41', 1699771308310499328, '2023-09-10 22:51:35', 0, NULL);

-- System Management Child Menus
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1699771910151151618, 'Menu Management', 'system:admin:menu', '/admin/sys-menu-manager', 1393000177613242368, 1, 1, 0, '3', 0, 'Menu', 65, 1699771308310499328, '2022-03-08 05:57:24', 1699771308310499328, '2025-07-10 22:46:08', 0, NULL),
(1150130227206176768, 'Role Management', ':admin:role-manager', '/admin/sys-role-manager', 1393000177613242368, 1, 1, 0, '', 0, 'UserFilled', 60, 1699771308310499328, '2023-09-09 18:06:55', 1699771308310499328, '2025-12-08 12:11:56', 0, NULL),
(1150191026649972736, 'User Management', ':admin:user-manager', '/admin/sys-user-manager', 1393000177613242368, 1, 1, 0, '', 0, 'User', 70, 1699771308310499328, '2023-09-09 22:08:30', 1699771308310499328, '2025-07-10 22:45:59', 0, NULL),
(1150266364558065664, 'Permission Management', ':admin:permission-manager', '/admin/sys-permission-manager', 1393000177613242368, 1, 1, 0, '', 0, 'Operation', 63, 1699771308310499328, '2023-09-10 03:07:52', 1699771308310499328, '2025-07-12 00:05:37', 0, NULL),
(1150464392514519040, 'Dictionary Management', ':admin:dictionary-manager', '/admin/sys-dictionary-manager', 1393000177613242368, 1, 1, 0, '', 0, 'Notebook', 50, 1699771308310499328, '2023-09-10 16:14:46', 1699771308310499328, '2025-07-11 22:48:11', 0, NULL),
(1150484898768764928, 'System Logs', ':admin:log-manager', '/admin/sys-log-manager', 1393000177613242368, 1, 1, 0, '', 0, 'Monitor', 10, 1699771308310499328, '2023-09-10 17:36:15', 1699771308310499328, '2025-12-08 12:12:03', 0, NULL);

-- VIP & SVIP (Top Level)
INSERT INTO sys_menu (id, menu_name, permission_code, router_path, parent_id, menu_type, is_menu, menu_status, component_path, is_external_link, icon_path, sort, create_by, create_time, update_by, update_time, is_deleted, revision) VALUES
(1255211293510893568, 'VIP', 'ps:vip', NULL, 0, 1, 0, 0, NULL, 0, NULL, 1, 1699771308310499328, '2024-06-25 17:21:33', 1699771308310499328, '2024-06-25 17:21:33', 0, NULL),
(1255199758382170112, 'SVIP', 'ps:svip', '', 0, 1, 0, 0, '', 0, '', 2, 1699771308310499328, '2024-06-25 16:35:43', 1699771308310499328, '2024-06-25 17:21:41', 0, NULL);
