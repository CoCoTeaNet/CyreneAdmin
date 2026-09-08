const AdminLayout = () => import("@/layout/AdminLayout.vue");
const Home = () => import("@/views/system/dashboard/Home.vue");
const UserView = () => import("@/views/system/manager/system/user/UserView.vue");
const MenuView = () => import("@/views/system/manager/system/menu/MenuView.vue");
const DictionaryView = () => import("@/views/system/manager/system/dictionary/DictionaryView.vue");
const PermissionView = () => import("@/views/system/manager/system/menu/PermissionView.vue");
const RoleView = () => import("@/views/system/manager/system/role/RoleView.vue");
const Dashboard = () => import("@/views/system/dashboard/Dashboard.vue");
const NotFound = () => import("@/views/error/NotFound.vue");
const UserCenterView = () => import("@/views/system/personal/UserCenterView.vue");
const OperationLogView = () => import("@/views/system/manager/system/log/SysLogView.vue");
const Login = () => import("@/views/system/login/Login.vue");


export const routes = [
    {
        path: "/login",
        name: "Login",
        meta: {title: 'route.login'},
        component: Login
    },
    {
        path: '/admin',
        name: 'Admin',
        meta: {title: 'route.admin'},
        component: AdminLayout,
        redirect: {name: 'Home'},
        children: [
            // 其它模块
            {path: 'home', meta: {title: 'route.home'}, name: 'Home', component: Home},
            {path: 'dashboard', meta: {title: 'route.dashboard'}, name: 'Dashboard', component: Dashboard},
            // 系统模块
            {path: 'sys-user-manager', meta: {title: 'route.userManager'}, name: 'UserView', component: UserView},
            {path: 'sys-menu-manager', meta: {title: 'route.menuManager'}, name: 'MenuView', component: MenuView},
            {path: 'sys-permission-manager', meta: {title: 'route.permissionManager'}, name: 'PermissionView', component: PermissionView},
            {path: 'sys-role-manager', meta: {title: 'route.roleManager'}, name: 'RoleView', component: RoleView},
            {path: 'sys-dictionary-manager', meta: {title: 'route.dictionaryManager'}, name: 'DictionaryView', component: DictionaryView},
            {path: 'sys-log-manager', meta: {title: 'route.logManager'}, name: 'OperationLogView', component: OperationLogView},
            {path: 'sys-user-center', meta: {title: 'route.userCenter'}, name: 'UserCenterView', component: UserCenterView},
        ]
    },
    {
        path: '/:pathMatch(.*)',
        name: 'error',
        component: NotFound,
        meta: {title: 'route.error'},
    }
];